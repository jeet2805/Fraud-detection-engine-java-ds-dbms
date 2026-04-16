package ui;

import db.DBConnection;
import service.AnalyticsService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;
    private final AnalyticsService analyticsService;

    public AdminMenu() {
        this.scanner = new Scanner(System.in);
        this.analyticsService = new AnalyticsService();
    }

    public void display() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n=== Admin Dashboard ===");
            System.out.println("Welcome, Master " + SessionManager.getCurrentUser().getUsername());
            System.out.println("1. View System Analytics");
            System.out.println("2. Manage Fraud Rules (Thresholds)");
            System.out.println("3. View System Audit Log");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1":
                        showAnalytics();
                        break;
                    case "2":
                        manageRules();
                        break;
                    case "3":
                        showAuditLog();
                        break;
                    case "4":
                        SessionManager.logout();
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAnalytics() throws SQLException {
        System.out.println("\n--- Real-Time System Analytics ---");
        Map<String, Object> stats = analyticsService.getSystemStats();
        System.out.println("Total Users: " + stats.get("total_users"));
        System.out.println("Successful Transactions: " + stats.get("successful_txns"));
        System.out.println("Flagged Transactions: " + stats.get("flagged_txns"));
        System.out.println("Blocked (Rolled Back): " + stats.get("blocked_txns"));
        System.out.println("Total System Volume: $" + (stats.get("total_volume") != null ? stats.get("total_volume") : "0.00"));
    }

    private void manageRules() throws SQLException {
        System.out.println("\n--- Fraud Rule Configuration ---");
        String sql = "SELECT rule_id, rule_name, threshold, is_active FROM rule_config";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.printf("[%d] %s | Threshold: %.2f | Active: %b%n",
                    rs.getInt("rule_id"), rs.getString("rule_name"),
                    rs.getBigDecimal("threshold"), rs.getBoolean("is_active"));
            }
        }
        
        System.out.print("Enter Rule ID to update threshold (0 to cancel): ");
        int rid = Integer.parseInt(scanner.nextLine());
        if (rid > 0) {
            System.out.print("Enter NEW threshold value: ");
            double newVal = Double.parseDouble(scanner.nextLine());
            String updateSql = "UPDATE rule_config SET threshold = ? WHERE rule_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, newVal);
                pstmt.setInt(2, rid);
                pstmt.executeUpdate();
                System.out.println("Rule updated successfully.");
            }
        }
    }

    private void showAuditLog() throws SQLException {
        System.out.println("\n--- System Audit Log (LIFO) ---");
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.printf("[%s] Action: %s | Entity: %s#%d | Reason: %s%n",
                    rs.getTimestamp("timestamp"), rs.getString("action"), 
                    rs.getString("entity_type"), rs.getInt("entity_id"), rs.getString("reason"));
            }
        }
    }
}
