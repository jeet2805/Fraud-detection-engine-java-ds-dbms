package dao;

import db.DBConnection;
import model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public int insertTransaction(int fromAccount, int toAccount, BigDecimal amount, String txnType, String status, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (from_account, to_account, amount, txn_type, status) VALUES (?, ?, ?, ?, ?) RETURNING txn_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (fromAccount > 0) stmt.setInt(1, fromAccount); else stmt.setNull(1, java.sql.Types.INTEGER);
            if (toAccount > 0) stmt.setInt(2, toAccount); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setBigDecimal(3, amount);
            stmt.setString(4, txnType);
            stmt.setString(5, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("txn_id");
                }
            }
        }
        throw new SQLException("Failed to insert transaction");
    }

    public List<Transaction> getRecentByAccount(int accountId, int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account = ? OR to_account = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getInt("txn_id"),
                        rs.getInt("from_account"),
                        rs.getInt("to_account"),
                        rs.getBigDecimal("amount"),
                        rs.getString("txn_type"),
                        rs.getString("status"),
                        rs.getTimestamp("timestamp")
                    ));
                }
            }
        }
        return transactions;
    }
}
