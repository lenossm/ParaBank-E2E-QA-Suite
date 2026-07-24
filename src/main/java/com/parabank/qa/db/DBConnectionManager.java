package com.parabank.qa.db;

import com.parabank.qa.config.ConfigReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * local h2 stand-in. cant connect to parabank's real db from outside.
 */
public class DBConnectionManager {

    private static Connection connection;

    private DBConnectionManager() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    ConfigReader.get("db.url"),
                    ConfigReader.get("db.user"),
                    ConfigReader.get("db.password", "")
            );
            initSchema(connection);
        }
        return connection;
    }

    private static void initSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ACCOUNT (
                        account_id   VARCHAR(32) PRIMARY KEY,
                        customer_id  VARCHAR(32),
                        account_type VARCHAR(32),
                        balance      DECIMAL(12, 2) NOT NULL
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS TRANSACTION (
                        transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        from_account_id VARCHAR(32) NOT NULL,
                        to_account_id   VARCHAR(32) NOT NULL,
                        amount          DECIMAL(12, 2) NOT NULL,
                        status          VARCHAR(32) NOT NULL
                    )
                    """);
        }
    }

    public static void upsertAccount(String accountId, String customerId, String type, double balance)
            throws SQLException {
        String sql = """
                MERGE INTO ACCOUNT (account_id, customer_id, account_type, balance)
                KEY (account_id)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, customerId);
            ps.setString(3, type);
            ps.setDouble(4, balance);
            ps.executeUpdate();
        }
    }

    public static void insertCompletedTransfer(String fromAccountId, String toAccountId, double amount)
            throws SQLException {
        String sql = """
                INSERT INTO TRANSACTION (from_account_id, to_account_id, amount, status)
                VALUES (?, ?, ?, 'COMPLETED')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, fromAccountId);
            ps.setString(2, toAccountId);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        }
    }

    public static boolean transferExists(String fromAccountId, String toAccountId, double amount, String status)
            throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM TRANSACTION
                WHERE from_account_id = ?
                  AND to_account_id = ?
                  AND amount = ?
                  AND status = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, fromAccountId);
            ps.setString(2, toAccountId);
            ps.setDouble(3, amount);
            ps.setString(4, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt") > 0;
            }
        }
    }

    public static Double getAccountBalance(String accountId) throws SQLException {
        String sql = "SELECT balance FROM ACCOUNT WHERE account_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
                return null;
            }
        }
    }

    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // whatever
            }
            connection = null;
        }
    }
}
