package rules;

import model.Transaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewAccountRule implements FraudRule {
    @Override
    public String getRuleName() { return "NEW_ACCOUNT_RISK"; }

    @Override
    public int evaluate(Transaction txn, Connection conn) {
        try {
            String sql = "SELECT created_at FROM accounts WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, txn.getFromAccount());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long accountAgeMillis = System.currentTimeMillis() - rs.getTimestamp("created_at").getTime();
                        long riskWindowMillis = 24 * 60 * 60 * 1000; // 24 hours

                        if (accountAgeMillis < riskWindowMillis && txn.getAmount().compareTo(new BigDecimal("1000")) > 0) {
                            return 30; // Risky for new account to send money immediately
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
