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

            accountDAO.updateBalance(accountId, amount, conn);
            transactionDAO.insertTransaction(-1, accountId, amount, "DEPOSIT", "SUCCESS", conn);

            conn.commit();
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

            Account sender = accountDAO.findByUserId(fromUserId);
            if (sender == null || sender.getBalance().compareTo(amount) < 0 || "FROZEN".equals(sender.getStatus())) {
                System.out.println("Transfer rejected: Account not found, insufficient funds, or frozen.");
                return false; 
            }

            // Create temporary transaction object for evaluation
            Transaction tempTxn = new Transaction(0, sender.getAccountId(), toAccountId, amount, "TRANSFER", "PENDING", null);

            // Step 1: Fraud Evaluation
            int riskScore = fraudService.evaluate(tempTxn, conn);
            String status = "SUCCESS";

            if (riskScore >= 70) {
                // BLOCK
                System.out.println("!!! TRANSACTION BLOCKED: Extreme Risk (" + riskScore + ") !!!");
                auditDAO.logAction("BLOCK", "ACCOUNT", sender.getAccountId(), fromUserId, "Risk Score: " + riskScore, conn);
                accountDAO.updateStatus(sender.getAccountId(), "FROZEN");
                transactionDAO.insertTransaction(sender.getAccountId(), toAccountId, amount, "TRANSFER", "ROLLED_BACK", conn);
                conn.commit(); // Save the block/freeze/log even though money didn't move
                return false;
            } else if (riskScore >= 30) {
                // FLAG
                status = "FLAGGED";
                System.out.println("??? TRANSACTION FLAGGED: Risk Score " + riskScore + " ???");
            }

            // Step 2: Execution
            accountDAO.updateBalance(sender.getAccountId(), amount.negate(), conn);
            accountDAO.updateBalance(toAccountId, amount, conn);
            int txnId = transactionDAO.insertTransaction(sender.getAccountId(), toAccountId, amount, "TRANSFER", status, conn);

            // Step 3: Alerts
            if (status.equals("FLAGGED")) {
                alertDAO.insertAlert(txnId, "MULTIPLE_RULES", riskScore, conn);
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public List<Transaction> getStatement(int accountId) throws SQLException {
        return transactionDAO.getRecentByAccount(accountId, 10);
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
