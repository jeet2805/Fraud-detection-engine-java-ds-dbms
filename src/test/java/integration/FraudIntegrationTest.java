package integration;

import service.AuthService;
import service.TransactionService;
import model.Account;
import model.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class FraudIntegrationTest {

    @Test
    public void testFraudDetectionFlows() throws SQLException {
        AuthService authService = new AuthService();
        TransactionService txnService = new TransactionService();

        // 1. Setup normal users
        String senderName = "sender_ph4_" + System.currentTimeMillis();
        String receiverName = "receiver_ph4_" + System.currentTimeMillis();
        
        authService.signup(senderName, "p", "CUSTOMER");
        authService.signup(receiverName, "p", "CUSTOMER");

        User sender = authService.login(senderName, "p");
        Account senderAcc = txnService.getAccountByUserId(sender.getUserId());
        Account receiverAcc = txnService.getAccountByUserId(authService.login(receiverName, "p").getUserId());

        // 2. Deposit enough for tests
        txnService.deposit(senderAcc.getAccountId(), new BigDecimal("100000.00"));

        // 3. Test LARGE_AMOUNT_RULE (Flagged but Success)
        // NewAccountRule adds 30, LargeAmountRule adds 40.
        // Let's send 5,000. This triggers NewAccountRule (30) but NOT LargeAmountRule (threshold 50k).
        // Total = 30 -> FLAGGED
        boolean flaggedSuccess = txnService.transfer(sender.getUserId(), receiverAcc.getAccountId(), new BigDecimal("5000.00"));
        assertTrue(flaggedSuccess, "Transfer of 5k should succeed but be FLAGGED in DB (check logs)");
        
        Account updatedSender = txnService.getAccountByUserId(sender.getUserId());
        assertEquals(new BigDecimal("95000.00").setScale(2), updatedSender.getBalance().setScale(2));

        // 4. Test BLOCK (Large Amount + New Account = 40 + 30 = 70)
        boolean largeBlocked = txnService.transfer(sender.getUserId(), receiverAcc.getAccountId(), new BigDecimal("60000.00"));
        assertFalse(largeBlocked, "Transfer of 60k for new account should be BLOCKED (70 risk)");

        // 5. Test VELOCITY_RULE (Flagged)
        // We've done 1 (deposit) + 1 (5k) + 1 (60k blocked) = 3 txns.
        // Let's do 3 more small transfers.
        for (int i = 0; i < 3; i++) {
            txnService.transfer(sender.getUserId(), receiverAcc.getAccountId(), new BigDecimal("10.00"));
        }
        // Next one should trigger velocity.

        // 6. Test CYCLE_DETECTION (Blocked + Rollback + Freeze)
        // A -> B, now B -> A
        User receiverUser = authService.login(receiverName, "p");
        // B sends back to A. This creates A -> B -> A.
        boolean cycleBlocked = txnService.transfer(receiverUser.getUserId(), senderAcc.getAccountId(), new BigDecimal("500.00"));
        assertFalse(cycleBlocked, "Cycle A->B->A should be BLOCKED");

        Account frozenAcc = txnService.getAccountByUserId(receiverUser.getUserId());
        assertEquals("FROZEN", frozenAcc.getStatus(), "Account should be frozen after blocking cycle");
    }
}
