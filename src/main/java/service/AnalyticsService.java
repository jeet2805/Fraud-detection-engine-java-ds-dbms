package service;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsService {
    
    public Map<String, Object> getSystemStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM users) as total_users, " +
                     "(SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS') as successful_txns, " +
                     "(SELECT COUNT(*) FROM transactions WHERE status = 'FLAGGED') as flagged_txns, " +
                     "(SELECT COUNT(*) FROM transactions WHERE status = 'ROLLED_BACK') as blocked_txns, " +
                     "(SELECT SUM(amount) FROM transactions WHERE status = 'SUCCESS') as total_vol";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("total_users", rs.getInt("total_users"));
                stats.put("successful_txns", rs.getInt("successful_txns"));
                stats.put("flagged_txns", rs.getInt("flagged_txns"));
                stats.put("blocked_txns", rs.getInt("blocked_txns"));
                stats.put("total_volume", rs.getBigDecimal("total_vol"));
            }
        }
        return stats;
    }
}
