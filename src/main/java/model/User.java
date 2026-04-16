package model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String role;
    private boolean isActive;
    private Timestamp createdAt;

    public User(int userId, String username, String passwordHash, String role, boolean isActive, Timestamp createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
