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
            System.out.println("4. View All System Accounts (Find IDs)");
            System.out.println("5. Global Reset: Clear ALL System Alerts");
            System.out.println("6. Logout");
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
                        accountDAO.listAllAccounts();
                        break;
                    case "5":
                        globalHistoryReset();
                        break;
                    case "6":
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
                System.out.printf("Account ID: %d | Name: %s | Balance: $%.2f%n",
                    rs.getInt("account_id"), rs.getString("holder_name"), rs.getBigDecimal("balance"));
            }
        }
        
        System.out.print("Enter Account ID to UNFREEZE (or 0 to go back): ");
        try {
            int accId = Integer.parseInt(scanner.nextLine());
            if (accId > 0) {
                int rows = accountDAO.updateStatus(accId, "ACTIVE", "FROZEN");
                if (rows > 0) {
                    // Mark all past fraud alerts for this account as reviewed
                    // AND insert dummy "reviewed" alerts for clean transactions 
                    // so cycle detection starts fresh – the account history is "cleared"
                    String clearExisting = "UPDATE fraud_alerts SET reviewed = TRUE " +
                                           "WHERE txn_id IN (SELECT txn_id FROM transactions WHERE from_account = ? OR to_account = ?)";
                    
                    String insertClearing = "INSERT INTO fraud_alerts (txn_id, rule_triggered, risk_score, reviewed, resolution) " +
                                            "SELECT txn_id, 'HISTORY_CLEARED', 0, TRUE, 'APPROVED' FROM transactions t " +
                                            "WHERE (from_account = ? OR to_account = ?) " +
                                            "AND NOT EXISTS (SELECT 1 FROM fraud_alerts fa WHERE fa.txn_id = t.txn_id)";
                    
                    try (Connection conn = DBConnection.getConnection()) {
                        conn.setAutoCommit(false);
                        try (PreparedStatement stmt1 = conn.prepareStatement(clearExisting);
                             PreparedStatement stmt2 = conn.prepareStatement(insertClearing)) {
                            
                            stmt1.setInt(1, accId);
                            stmt1.setInt(2, accId);
                            stmt1.executeUpdate();
                            
                            stmt2.setInt(1, accId);
                            stmt2.setInt(2, accId);
                            stmt2.executeUpdate();
                            
                            conn.commit();
                        } catch (SQLException e) {
                            conn.rollback();
                            throw e;
                        }
                    }
                    System.out.println("Account #" + accId + " has been UNFROZEN and all transaction edges cleared.");
                } else {
                    System.out.println("Error: Account #" + accId + " was not found or is not currently FROZEN.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Account ID must be a numeric value.");
        }
    }

    private void globalHistoryReset() throws SQLException {
        System.out.print("WARNING: This will clear ALL cycle detection edges globally. Proceed? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirm)) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Mark all existing alerts as reviewed
                    String updateSql = "UPDATE fraud_alerts SET reviewed = TRUE";
                    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.executeUpdate();
                    }

                    // 2. Insert reviewed alerts for every single transaction to clear graph
                    String insertSql = "INSERT INTO fraud_alerts (txn_id, rule_triggered, risk_score, reviewed, resolution) " +
                                       "SELECT txn_id, 'GLOBAL_RESET', 0, TRUE, 'APPROVED' FROM transactions t " +
                                       "WHERE NOT EXISTS (SELECT 1 FROM fraud_alerts fa WHERE fa.txn_id = t.txn_id)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    System.out.println("Global Reset complete. System history is now cleared for cycle detection.");
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        } else if ("n".equals(confirm)) {
            System.out.println("Global Reset stopped.");
        } else {
            System.out.println("Invalid answer. Reset aborted.");
        }
    }
}
