package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditDAO {
    public void logAction(String action, String entityType, int entityId, int performedBy, String reason, Connection conn) throws SQLException {
        String sql = "INSERT INTO audit_log (action, entity_type, entity_id, performed_by, reason) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setString(2, entityType);
            stmt.setInt(3, entityId);
            if (performedBy > 0) stmt.setInt(4, performedBy); else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setString(5, reason);
            stmt.executeUpdate();
        }
    }
}
