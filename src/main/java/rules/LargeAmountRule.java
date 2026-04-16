package rules;

import model.Transaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LargeAmountRule implements FraudRule {
    @Override
    public String getRuleName() { return "LARGE_AMOUNT_CHECK"; }

    @Override
    public int evaluate(Transaction txn, Connection conn) {
        try {
            String sql = "SELECT threshold FROM rule_config WHERE rule_name = 'LARGE_AMOUNT_THRESHOLD' AND is_active = true";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal threshold = rs.getBigDecimal("threshold");
                    if (txn.getAmount().compareTo(threshold) > 0) {
                        return 40; // High risk score
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
