package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AlertDAO {
    public void insertAlert(int txnId, String ruleTriggered, int riskScore, Connection conn) throws SQLException {
        String sql = "INSERT INTO fraud_alerts (txn_id, rule_triggered, risk_score) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, txnId);
            stmt.setString(2, ruleTriggered);
            stmt.setInt(3, riskScore);
            stmt.executeUpdate();
        }
    }

    public java.util.List<String> getAlertsByAccount(int accountId) throws SQLException {
        java.util.List<String> alerts = new java.util.ArrayList<>();
        String sql = "SELECT fa.rule_triggered, fa.risk_score, fa.created_at " +
                     "FROM fraud_alerts fa " +
                     "JOIN transactions t ON fa.txn_id = t.txn_id " +
                     "WHERE t.from_account = ? ORDER BY fa.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(String.format("[%s] ALERT: %s (Risk Score: %d)",
                        rs.getTimestamp("created_at"), rs.getString("rule_triggered"), rs.getInt("risk_score")));
                }
            }
        }
        return alerts;
    }
}
