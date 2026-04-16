package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import db.DBConnection;
import model.Account;

public class AccountDAO {
    public void insertAccount(int userId, String holderName, Connection conn) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, holder_name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, holderName);
            stmt.executeUpdate();
        }
    }

    public Account findById(int accountId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("holder_name"),
                        rs.getBigDecimal("balance"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public Account findById(int accountId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(accountId, conn);
        }
    }

    public Account findByUserId(int userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("holder_name"),
                        rs.getBigDecimal("balance"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public Account findByUserId(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByUserId(userId, conn);
        }
    }

    public void updateBalance(int accountId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    public int updateStatus(int accountId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, accountId);
            return stmt.executeUpdate();
        }
    }

    public void updateStatus(int accountId, String status) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            updateStatus(accountId, status, conn);
        }
    }
}
