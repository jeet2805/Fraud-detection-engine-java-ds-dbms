package rules;

import model.Transaction;
import java.sql.Connection;

public interface FraudRule {
    String getRuleName();
    int evaluate(Transaction txn, Connection conn);
}
