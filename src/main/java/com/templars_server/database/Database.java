package com.templars_server.database;

import com.templars_server.Application;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final HikariDataSource dataSource;

    public Database(String address, String table, String user, String password) {
        HikariConfig config = new HikariConfig();
        String uri = String.format("jdbc:mariadb://%s/%s", address, table);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(uri);
        config.setUsername(user);
        config.setPassword(password);
        this.dataSource = new HikariDataSource(config);
    }

    public void setup() throws SQLException {
        Connection db = dataSource.getConnection();
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
        try (Connection db = dataSource.getConnection()) {
            String sql = "SELECT vpn, vpn_check FROM anti_vpn WHERE ip = ?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, ip);
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    return new AntiVPNRow(ip, result.getBoolean(1), result.getBoolean(2));
                }
            }
        }

        return null;
    }

    public void insertRow(AntiVPNRow antiVPNRow) throws SQLException {
        try (Connection db = dataSource.getConnection()) {
            String sql = "INSERT IGNORE INTO anti_vpn (ip, vpn, vpn_check) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, antiVPNRow.getIp());
                stmt.setInt(2, antiVPNRow.isVpn() ? 1 : 0);
                stmt.setInt(3, antiVPNRow.isVpnCheck() ? 1 : 0);
                stmt.executeUpdate();
            }
        }
    }

}
