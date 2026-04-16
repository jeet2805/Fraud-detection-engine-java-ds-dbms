package service;

import ds.RuleEngineStack;
import model.Transaction;
import rules.*;

import java.sql.Connection;

public class FraudDetectionService {
    private final RuleEngineStack ruleStack;

    public FraudDetectionService() {
        this.ruleStack = new RuleEngineStack(10);
        // Load all active rules
        ruleStack.push(new LargeAmountRule());
        ruleStack.push(new VelocityRule());
        ruleStack.push(new CycleDetectionRule());
        ruleStack.push(new NewAccountRule());
    }

    public int evaluate(Transaction txn, Connection conn) {
        int totalScore = 0;
        for (int i = 0; i < ruleStack.size(); i++) {
            FraudRule rule = (FraudRule) ruleStack.getAt(i);
            int score = rule.evaluate(txn, conn);
            totalScore += score;
            if (score > 0) {
                System.out.println("[FRAUD_ENGINE] Rule Triggered: " + rule.getRuleName() + " (Score: " + score + ")");
            }
        }
        return totalScore;
    }
}
