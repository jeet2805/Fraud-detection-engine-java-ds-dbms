package service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import model.Account;
import model.Transaction;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {

    @Test
    public void testDepositAndTransfer() throws SQLException {
        AuthService authService = new AuthService();
        TransactionService txnService = new TransactionService();

        String userA = "sender_" + System.currentTimeMillis();
        String userB = "receiver_" + System.currentTimeMillis();

        authService.signup(userA, "pass", "CUSTOMER");
        authService.signup(userB, "pass", "CUSTOMER");

        int userIdA = authService.login(userA, "pass").getUserId();
        int userIdB = authService.login(userB, "pass").getUserId();

        Account accA = txnService.getAccountByUserId(userIdA);
        Account accB = txnService.getAccountByUserId(userIdB);

        assertNotNull(accA);
        assertNotNull(accB);

        // Test Deposit
        boolean depositSuccess = txnService.deposit(accA.getAccountId(), new BigDecimal("10000.00"));
        assertTrue(depositSuccess);

        Account updatedAccA = txnService.getAccountByUserId(userIdA);
        assertTrue(new BigDecimal("10000.00").compareTo(updatedAccA.getBalance()) == 0);

        // Test Transfer
        boolean transferSuccess = txnService.transfer(userIdA, accB.getAccountId(), new BigDecimal("1000.00"));
        assertTrue(transferSuccess);

        updatedAccA = txnService.getAccountByUserId(userIdA);
        Account updatedAccB = txnService.getAccountByUserId(userIdB);

        assertTrue(new BigDecimal("9000.00").compareTo(updatedAccA.getBalance()) == 0);
        assertTrue(new BigDecimal("1000.00").compareTo(updatedAccB.getBalance()) == 0);
        
        // Test Statement
        List<Transaction> statements = txnService.getStatement(accA.getAccountId());
        assertFalse(statements.isEmpty());
    }
}
