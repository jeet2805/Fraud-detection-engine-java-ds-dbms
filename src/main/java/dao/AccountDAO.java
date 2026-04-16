package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AccountDAO {
    public void insertAccount(int userId, String holderName, Connection conn) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, holder_name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, holderName);
            stmt.executeUpdate();
        }
    }
}
