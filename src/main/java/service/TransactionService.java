package service;

import dao.AccountDAO;
import dao.AlertDAO;
import dao.AuditDAO;
import dao.TransactionDAO;
import db.DBConnection;
import model.Account;
import model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TransactionService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final FraudDetectionService fraudService;
    private final AlertDAO alertDAO;
    private final AuditDAO auditDAO;

    public TransactionService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.fraudService = new FraudDetectionService();
        this.alertDAO = new AlertDAO();
        this.auditDAO = new AuditDAO();
    }

    public Account getAccountByUserId(int userId) throws SQLException {
        return accountDAO.findByUserId(userId);
    }

    public boolean deposit(int accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Account account = accountDAO.findById(accountId, conn);
            if (account == null) {
                System.out.println("Deposit rejected: Account not found.");
                return false;
            }

            // 1. Evaluate Fraud
            Transaction probe = new Transaction(0, -1, accountId, amount, "DEPOSIT", "PENDING", null);
            int riskScore = fraudService.evaluate(probe, conn);
            
            String finalStatus = "SUCCESS";

            if (riskScore >= 70) {
                // BLOCK
                finalStatus = "ROLLED_BACK";
                System.out.println("!!! DEPOSIT BLOCKED: Risk Score " + riskScore + " !!!");
                
                // Freeze account
                accountDAO.updateStatus(accountId, "FROZEN", "ACTIVE");
                auditDAO.logAction("FREEZE", "ACCOUNT", accountId, -1, "Blocked deposit risk: " + riskScore, conn);
                
                // Record attempt
                int tid = transactionDAO.insertTransaction(-1, accountId, amount, "DEPOSIT", finalStatus, conn);
                alertDAO.insertAlert(tid, "FRAUD_BLOCK_DEPOSIT", riskScore, conn);
                
                conn.commit();
                return false;
            } else if (riskScore >= 30) {
                finalStatus = "FLAGGED";
            }

            // 2. Execute
            accountDAO.updateBalance(accountId, amount, conn);
            int tid = transactionDAO.insertTransaction(-1, accountId, amount, "DEPOSIT", finalStatus, conn);
            
            if (finalStatus.equals("FLAGGED")) {
                alertDAO.insertAlert(tid, "DEPOSIT_FLAG", riskScore, conn);
            }

            conn.commit();
            if (finalStatus.equals("FLAGGED")) {
                System.out.println("Deposit " + tid + " flagged with status: FLAGGED (Risk: " + riskScore + ")");
            }
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean transfer(int fromUserId, int toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Fetch SHARP data within the transaction
            Account sender = accountDAO.findByUserId(fromUserId, conn);
            Account recipient = accountDAO.findById(toAccountId, conn);

            // Validations
            if (sender == null) {
                System.out.println("Transfer rejected: Sender account not found.");
                return false;
            }
            if ("FROZEN".equals(sender.getStatus())) {
                System.out.println("Transfer rejected: Your account is FROZEN.");
                return false;
            }
            if (sender.getBalance().compareTo(amount) < 0) {
                System.out.println("Transfer rejected: Insufficient funds (Balance: $" + sender.getBalance() + ")");
                return false;
            }
            if (recipient == null) {
                System.out.println("Transfer rejected: Recipient ID #" + toAccountId + " not found.");
                return false;
            }
            if (sender.getAccountId() == recipient.getAccountId()) {
                System.out.println("Transfer rejected: Cannot transfer to yourself.");
                return false;
            }

            // 1. Evaluate Fraud
            Transaction probe = new Transaction(0, sender.getAccountId(), toAccountId, amount, "TRANSFER", "PENDING", null);
            int riskScore = fraudService.evaluate(probe, conn);
            
            String finalStatus = "SUCCESS";

            if (riskScore >= 70) {
                // BLOCK
                finalStatus = "ROLLED_BACK";
                System.out.println("!!! BLOCKED: Risk Score " + riskScore + " !!!");
                
                // Freeze sender
                accountDAO.updateStatus(sender.getAccountId(), "FROZEN", conn);
                auditDAO.logAction("FREEZE", "ACCOUNT", sender.getAccountId(), fromUserId, "Blocked txn risk: " + riskScore, conn);
                
                // Record the failed attempt
                int tid = transactionDAO.insertTransaction(sender.getAccountId(), toAccountId, amount, "TRANSFER", finalStatus, conn);
                alertDAO.insertAlert(tid, "FRAUD_BLOCK", riskScore, conn);
                
                conn.commit();
                return false;
            } else if (riskScore >= 30) {
                // FLAG but proceed
                finalStatus = "FLAGGED";
            }

            // 2. Execute Balances
            accountDAO.updateBalance(sender.getAccountId(), amount.negate(), conn);
            accountDAO.updateBalance(recipient.getAccountId(), amount, conn);
            
            // 3. Record Transaction
            int tid = transactionDAO.insertTransaction(sender.getAccountId(), toAccountId, amount, "TRANSFER", finalStatus, conn);
            
            if (finalStatus.equals("FLAGGED")) {
                alertDAO.insertAlert(tid, "MULTIPLE_RULES", riskScore, conn);
            }

            conn.commit();
            System.out.println("Transaction " + tid + " committed with status: " + finalStatus);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Database Error during transfer: " + e.getMessage());
            rollback(conn);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public List<Transaction> getStatement(int accountId) throws SQLException {
        return transactionDAO.getRecentByAccount(accountId, 10);
    }

    public List<String> getAlerts(int accountId) throws SQLException {
        return alertDAO.getAlertsByAccount(accountId);
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true); // Reset for pool safety
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
