package ui;

import dao.AccountDAO;
import db.DBConnection;
import ds.MinHeap;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AnalystMenu {
    private final Scanner scanner;
    private final AccountDAO accountDAO;

    public AnalystMenu() {
        this.scanner = new Scanner(System.in);
        this.accountDAO = new AccountDAO();
    }

    public void display() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n=== Analyst Dashboard ===");
            System.out.println("Welcome, " + SessionManager.getCurrentUser().getUsername());
            System.out.println("1. View Priority Fraud Alerts (High Risk First)");
            System.out.println("2. View All Flagged Transactions");
            System.out.println("3. Manage Frozen Accounts");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1":
                        showPriorityAlerts();
                        break;
                    case "2":
                        showFlaggedTransactions();
                        break;
                    case "3":
                        manageFrozenAccounts();
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

    private void showPriorityAlerts() throws SQLException {
        System.out.println("\n--- Priority Alerts (Using Custom Heap) ---");
        // We use the MinHeap (Max-Priority logic) to sort alerts by risk score
        MinHeap heap = new MinHeap(100);
        
        String sql = "SELECT risk_score FROM fraud_alerts WHERE reviewed = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                heap.insert(rs.getInt("risk_score"));
            }
        }

        if (heap.size() == 0) {
            System.out.println("No pending alerts.");
            return;
        }

        System.out.println("Active Risk Scores in Queue:");
        while (heap.size() > 0) {
            System.out.println("-> Risk Score: [" + heap.extractMax() + "]");
        }
    }

    private void showFlaggedTransactions() throws SQLException {
        System.out.println("\n--- Flagged Transactions ---");
        String sql = "SELECT * FROM transactions WHERE status = 'FLAGGED' ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.printf("TXN #%d | From: %d | To: %d | Amount: $%.2f | Time: %s%n",
                    rs.getInt("txn_id"), rs.getInt("from_account"), rs.getInt("to_account"),
                    rs.getBigDecimal("amount"), rs.getTimestamp("timestamp"));
            }
        }
    }

    private void manageFrozenAccounts() throws SQLException {
        System.out.println("\n--- Frozen Accounts ---");
        String sql = "SELECT account_id, holder_name, balance FROM accounts WHERE status = 'FROZEN'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Balance: $%.2f%n",
                    rs.getInt("account_id"), rs.getString("holder_name"), rs.getBigDecimal("balance"));
            }
        }
        
        System.out.print("Enter Account ID to UNFREEZE (or 0 to go back): ");
        try {
            int accId = Integer.parseInt(scanner.nextLine());
            if (accId > 0) {
                int rows = accountDAO.updateStatus(accId, "ACTIVE");
                if (rows > 0) {
                    System.out.println("Account #" + accId + " has been UNFROZEN.");
                } else {
                    System.out.println("Error: Account #" + accId + " was not found or is not frozen.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Account ID must be a numeric value.");
        }
    }
}
