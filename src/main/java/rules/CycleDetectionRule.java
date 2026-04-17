package rules;

import ds.TransactionGraph;
import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CycleDetectionRule implements FraudRule {
    @Override
    public String getRuleName() { return "CYCLE_DETECTION"; }

    @Override
    public int evaluate(Transaction txn, Connection conn) {
        if (txn.getFromAccount() <= 0 || txn.getToAccount() <= 0) return 0;
        if (txn.getFromAccount() == txn.getToAccount()) return 0;

        try {
            // Step 1: Get the max account ID to size the graph correctly
            int maxAccountId = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT MAX(account_id) FROM accounts");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) maxAccountId = rs.getInt(1);
            }
            
            // If the graph is too small, we can't detect cycles reliably
            if (maxAccountId <= 0) return 0;
            
            TransactionGraph graph = new TransactionGraph(maxAccountId + 1);
            int edgeCount = 0;

            // Step 2: Load historical transfers
            // We only care about transactions that haven't been "reviewed/cleared"
            // The "fresh start" logic exclusion:
            String sql = "SELECT from_account, to_account FROM transactions t " +
                         "WHERE from_account IS NOT NULL AND to_account IS NOT NULL " +
                         "AND status IN ('SUCCESS', 'FLAGGED') " +
                         "AND NOT EXISTS ( " +
                         "  SELECT 1 FROM fraud_alerts fa " +
                         "  WHERE fa.txn_id = t.txn_id AND fa.reviewed = TRUE " +
                         ")";
            
            int totalInDB = 0;
            String countSql = "SELECT COUNT(*) FROM transactions WHERE status IN ('SUCCESS', 'FLAGGED')";
            try (PreparedStatement stmt = conn.prepareStatement(countSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) totalInDB = rs.getInt(1);
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    graph.addEdge(rs.getInt("from_account"), rs.getInt("to_account"));
                    edgeCount++;
                }
            }

            if (edgeCount < totalInDB) {
                System.out.println("[DEBUG] Cycle Engine: Loaded " + edgeCount + " active edges (Skipped " + (totalInDB - edgeCount) + " cleared transactions).");
            }

            // Step 3: Add the current proposed edge
            graph.addEdge(txn.getFromAccount(), txn.getToAccount());

            // Check for cycle
            if (graph.hasCycle(txn.getFromAccount())) {
                System.out.println("[DEBUG] Cycle detected using " + (edgeCount + 1) + " edges.");
                return 80; // High risk score
            }

        } catch (SQLException e) {
            System.err.println("Error in CycleDetectionRule: " + e.getMessage());
        }
        return 0;
    }
}
