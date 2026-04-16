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
}
