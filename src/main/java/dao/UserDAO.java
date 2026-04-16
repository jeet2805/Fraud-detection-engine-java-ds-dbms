package dao;

import db.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {
    
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null; // Not found
    }

    // Insert user, passing conn for transaction management
    public int insertUser(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?) RETURNING user_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        throw new SQLException("Inserting user failed, no ID obtained.");
    }

    public void logLogin(int userId, String status) throws SQLException {
        String sql = "INSERT INTO login_log (user_id, status) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId > 0) stmt.setInt(1, userId);
            else stmt.setNull(1, java.sql.Types.INTEGER);
            stmt.setString(2, status);
            stmt.executeUpdate();
        }
    }
}
