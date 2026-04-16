package service;

import dao.AccountDAO;
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

    public TransactionService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
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

            Account acc = accountDAO.findByUserId(accountId); // Using find by UserID for simplicity in this system since 1:1, wait we need findByAccountId or findByUserId
            // Let's assume the user calls findByUserId to get their own account first
            
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
                return false; // Insufficient funds, frozen or not found
            }
            
            // Deduct sender
            accountDAO.updateBalance(sender.getAccountId(), amount.negate(), conn);
            // Add receiver
            accountDAO.updateBalance(toAccountId, amount, conn);
            // Insert transaction row
            transactionDAO.insertTransaction(sender.getAccountId(), toAccountId, amount, "TRANSFER", "SUCCESS", conn);

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
