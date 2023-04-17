package com.templars_server;


import com.templars_server.database.AntiVPNRow;
import com.templars_server.database.Database;
import com.templars_server.iphub.IPHub;
import com.templars_server.mb2_log_reader.schema.ClientConnectEvent;
import com.templars_server.mb2_log_reader.schema.ClientDisconnectEvent;
import com.templars_server.mb2_log_reader.schema.ClientSpawnedEvent;
import com.templars_server.mb2_log_reader.schema.ShutdownGameEvent;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.whitelist.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AntiVPN {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final IPHub ipHub;
    private final Database database;
    private final RconClient rcon;
    private final Whitelist whitelist;
    private final boolean delayedBan;
    private final Map<Integer, ClientConnectEvent> banned;

    public AntiVPN(IPHub ipHub, Database database, RconClient rcon, Whitelist whitelist, boolean delayedBan) {
        this.ipHub = ipHub;
        this.database = database;
        this.rcon = rcon;
        this.whitelist = whitelist;
        this.delayedBan = delayedBan;
        this.banned = new HashMap<>();
    }

    void onClientSpawnedEvent(ClientSpawnedEvent event) {
        if (delayedBan) {
            banClient(event.getSlot());
        }
    }

    void onClientConnectEvent(ClientConnectEvent event) {
        if (whitelist.contains(event.getIp())) {
            LOG.debug("Skipping onConnect check, IP " + event.getIp() + " is whitelisted");
            return;
        }

        LOG.debug("New connection " + event.getIp());
        try {
            AntiVPNRow row = database.getRowByIp(event.getIp());
            if (row == null) {
                LOG.info(String.format("VPN checking (ip=%s, name=%s)", event.getIp(), event.getName()));
                row = new AntiVPNRow(
                        event.getIp(),
                        ipHub.checkIp(event.getIp()),
                        true
                );
                database.insertRow(row);
            }

            if (whitelist.contains(row.getIp())) {
                return;
            }

            if (row.isVpn()) {
                LOG.info(String.format("Address marked as proxy or VPN (ip=%s)", event.getIp()));
                banned.put(event.getSlot(), event);
                LOG.info("Ban list: " + banned);
            }

            if (!delayedBan) {
                banClient(event.getSlot());
            }
        } catch (IOException | URISyntaxException | InterruptedException | SQLException e) {
            LOG.error("Couldn't process connection", e);
        }
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        banned.remove(event.getSlot());
    }

    public void onShutdownGame(ShutdownGameEvent event) {
        banned.clear();
    }

    private void banClient(int slot) {
        ClientConnectEvent client = banned.get(slot);
        if (client != null) {
            LOG.info(String.format("Banning (slot=%s, ip=%s, alias=%s)", client.getSlot(), client.getIp(), client.getName()));
            rcon.print(client.getSlot(), "^1Anti-VPN » !!! WARNING !!! ^3You have been marked for VPN usage, to prevent abuse we ban VPNs and proxies.");
            rcon.printConAll(String.format("^1Anti-VPN »^7 Banning %s^7 for VPN or proxy usage", client.getName()));
            LOG.info("Rcon addip: " +  rcon.addIp(client.getIp()).replace("\n", ""));
            LOG.info("Rcon kick: " +  rcon.kick(client.getSlot()).replace("\n", ""));
            banned.remove(client.getSlot());
            LOG.info("Ban list: " + banned);
        }
    }

}
