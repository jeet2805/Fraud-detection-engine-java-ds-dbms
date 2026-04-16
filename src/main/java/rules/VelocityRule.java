package rules;

import ds.SlidingWindowDeque;
import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VelocityRule implements FraudRule {
    @Override
    public String getRuleName() { return "VELOCITY_CHECK"; }

    @Override
    public int evaluate(Transaction txn, Connection conn) {
        SlidingWindowDeque deque = new SlidingWindowDeque();
        try {
            // Get last few transaction timestamps for this account
            String sql = "SELECT timestamp FROM transactions WHERE from_account = ? ORDER BY timestamp DESC LIMIT 10";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, txn.getFromAccount());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        deque.addLast(rs.getTimestamp("timestamp").getTime());
                    }
                }
            }

            // Get velocity settings from DB
            int maxTxn = 5;
            int windowSec = 60;
            String configSql = "SELECT rule_name, threshold FROM rule_config WHERE rule_name LIKE 'VELOCITY_%'";
            try (PreparedStatement stmt = conn.prepareStatement(configSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if ("VELOCITY_MAX_TXN".equals(rs.getString("rule_name"))) maxTxn = rs.getBigDecimal("threshold").intValue();
                    if ("VELOCITY_WINDOW_SEC".equals(rs.getString("rule_name"))) windowSec = rs.getBigDecimal("threshold").intValue();
                }
            }

            long now = System.currentTimeMillis();
            deque.addLast(now); // Add current txn
            deque.evictExpired(now, windowSec);

            if (deque.count() > maxTxn) {
                return 35; // Velocity triggered
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
