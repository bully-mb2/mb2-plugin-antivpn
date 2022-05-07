package com.templars_server.database;

import com.templars_server.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final String address;
    private final String table;
    private final String user;
    private final String password;
    private Connection db;

    public Database(String address, String table, String user, String password) {
        this.address = address;
        this.table = table;
        this.user = user;
        this.password = password;
    }

    public void setup() throws SQLException {
        checkConnection();

        LOG.info("Creating table anti_vpn if it doesn't already exist");
        try (Statement stmt = db.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `anti_vpn` (" +
                    "    `ip` varchar(15) NOT NULL," +
                    "    `vpn` tinyint(1) DEFAULT 0," +
                    "    `vpn_check` tinyint(1) DEFAULT 0," +
                    "    PRIMARY KEY (`ip`)" +
                    ");"
            );
        }
    }

    public AntiVPNRow getRowByIp(String ip) throws SQLException {
        checkConnection();

        String sql = "SELECT vpn, vpn_check FROM anti_vpn WHERE ip = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, ip);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return new AntiVPNRow(ip, result.getBoolean(1), result.getBoolean(2));
            }
        }

        return null;
    }

    public void insertRow(AntiVPNRow antiVPNRow) throws SQLException {
        checkConnection();

        String sql = "INSERT IGNORE INTO anti_vpn (ip, vpn, vpn_check) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, antiVPNRow.getIp());
            stmt.setInt(2, antiVPNRow.isVpn() ? 1 : 0);
            stmt.setInt(3, antiVPNRow.isVpnCheck() ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    private void checkConnection() throws SQLException {
        if (db == null || db.isClosed()) {
            connect();
        }
    }

    private void connect() throws SQLException {
        if (db != null) {
            db.close();
        }

        String uri = String.format("jdbc:mariadb://%s/%s", address, table);
        LOG.info("Connecting to database " + uri);
        db = DriverManager.getConnection(
                uri,
                user,
                password
        );

        LOG.info("Successfully connected to database");
    }

}
