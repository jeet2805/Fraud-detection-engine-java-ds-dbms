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

        try {
            // Build temporary graph from recent transactions
            TransactionGraph graph = new TransactionGraph(1000); // Simple hash-based adjacency
            String sql = "SELECT from_account, to_account FROM transactions WHERE timestamp > NOW() - INTERVAL '1 hour'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    graph.addEdge(rs.getInt("from_account"), rs.getInt("to_account"));
                }
            }

            // Add the proposed transaction
            graph.addEdge(txn.getFromAccount(), txn.getToAccount());

            // Check if it creates a cycle involving the sender
            if (graph.hasCycle(txn.getFromAccount())) {
                return 80; // Extremely high risk - usually money laundering
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
