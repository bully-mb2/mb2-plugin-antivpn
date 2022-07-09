package com.templars_server;


import com.templars_server.database.AntiVPNRow;
import com.templars_server.database.Database;
import com.templars_server.iphub.IPHub;
import com.templars_server.util.rcon.RconClient;
import generated.ClientConnectEvent;
import generated.ClientDisconnectEvent;
import generated.ClientSpawnedEvent;
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
    private final Map<Integer, ClientConnectEvent> banned;

    public AntiVPN(IPHub ipHub, Database database, RconClient rcon) {
        this.ipHub = ipHub;
        this.database = database;
        this.rcon = rcon;
        this.banned = new HashMap<>();
    }

    void onClientSpawnedEvent(ClientSpawnedEvent event) {
        ClientConnectEvent client = banned.get(event.getSlot());
        if (client != null) {
            LOG.info(String.format("Banning (slot=%s, ip=%s, alias=%s)", event.getSlot(), client.getIp(), client.getName()));
            rcon.print(event.getSlot(), "^1Anti-VPN » !!! WARNING !!! ^3You have been marked for VPN usage, to prevent abuse we ban VPNs and proxies.");
            rcon.printConAll(String.format("^1Anti-VPN »^7 Banning %s^7 for VPN or proxy usage", client.getName()));
            LOG.info("Rcon ban: " +  rcon.ban(client.getSlot()).replace("\n", ""));
            banned.remove(event.getSlot());
            LOG.info("Ban list: " + banned);
        }
    }

    void onClientConnectEvent(ClientConnectEvent event) {
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

            if (row.isVpn()) {
                LOG.info(String.format("Address marked as proxy or VPN (ip=%s)", event.getIp()));
                banned.put(event.getSlot(), event);
                LOG.info("Ban list: " + banned);
            }
        } catch (IOException | URISyntaxException | InterruptedException | SQLException e) {
            LOG.error("Couldn't process connection", e);
        }
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        banned.remove(event.getSlot());
    }

}
