package com.templars_server.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
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
    private final ComboPooledDataSource pool;

    public Database(String address, String table, String user, String password) {
        this.address = address;
        this.table = table;
        this.user = user;
        this.password = password;
        this.pool = new ComboPooledDataSource();
        String uri = String.format("jdbc:mariadb://%s/%s", address, table);
        pool.setJdbcUrl(uri);
        pool.setUser(user);
        pool.setPassword(password);
    }

    public void setup() throws SQLException {
        Connection db = pool.getConnection();
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
        Connection db = pool.getConnection();
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
        Connection db = pool.getConnection();
        String sql = "INSERT IGNORE INTO anti_vpn (ip, vpn, vpn_check) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, antiVPNRow.getIp());
            stmt.setInt(2, antiVPNRow.isVpn() ? 1 : 0);
            stmt.setInt(3, antiVPNRow.isVpnCheck() ? 1 : 0);
            stmt.executeUpdate();
        }
    }

}
