package com.templars_server;

import com.templars_server.database.Database;
import com.templars_server.iphub.IPHub;
import com.templars_server.util.mqtt.MBMqttClient;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.util.settings.Settings;
import generated.ClientConnectEvent;
import generated.ClientDisconnectEvent;
import generated.ClientSpawnedEvent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException, MqttException, SQLException {
        LOG.info("======== Starting mb2-plugin-anti-vpn ========");
        LOG.info("Loading settings");
        Settings settings = new Settings();
        settings.load("application.properties");

        LOG.info("Setting up database connection");
        Database database = new Database(
                settings.get("database.address"),
                settings.get("database.table"),
                settings.get("database.user"),
                settings.get("database.password")
        );
        database.setup();

        LOG.info("Setting up rcon client");
        RconClient rcon = new RconClient();
        rcon.connect(
                settings.getAddress("rcon.host"),
                settings.get("rcon.password")
        );

        LOG.info("Setting up Anti VPN");
        IPHub ipHub = new IPHub(settings.get("iphub.apikey"));
        AntiVPN antiVPN = new AntiVPN(ipHub, database, rcon);

        LOG.info("Registering event callbacks");
        MBMqttClient client = new MBMqttClient();
        client.putEventListener(antiVPN::onClientSpawnedEvent, ClientSpawnedEvent.class);
        client.putEventListener(antiVPN::onClientConnectEvent, ClientConnectEvent.class);
        client.putEventListener(antiVPN::onClientDisconnectEvent, ClientDisconnectEvent.class);

        LOG.info("Connecting to MQTT broker");
        client.connect(
                "tcp://localhost:" + settings.getInt("mqtt.port"),
                settings.get("mqtt.topic")
        );
    }

}
