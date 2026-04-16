package service;

import model.User;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    public void testHashPassword() {
        AuthService authService = new AuthService();
        String hash1 = authService.hashPassword("mySecurePassword123");
        String hash2 = authService.hashPassword("mySecurePassword123");
        String hash3 = authService.hashPassword("differentPassword");

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Same passwords should have identical hashes.");
        assertNotEquals(hash1, hash3, "Different passwords should have diff hashes.");
    }

    @Test
    public void testSignupAndLoginFlow() throws SQLException {
        AuthService authService = new AuthService();
        String testUser = "testUser_" + System.currentTimeMillis();
        String testPass = "password";

        // Test Signup
        boolean signupSuccess = authService.signup(testUser, testPass, "CUSTOMER");
        assertTrue(signupSuccess, "Signup should succeed for new user.");

        // Duplicate signup
        boolean duplicateSuccess = authService.signup(testUser, "diffPass", "CUSTOMER");
        assertFalse(duplicateSuccess, "Signup should fail for duplicate username.");

        // Test Login
        User loggedInUser = authService.login(testUser, testPass);
        assertNotNull(loggedInUser, "Login should succeed with correct credentials.");
        assertEquals(testUser, loggedInUser.getUsername());
        assertEquals("CUSTOMER", loggedInUser.getRole());

        // Test Failed Login
        User failedLogin = authService.login(testUser, "wrongPassword");
        assertNull(failedLogin, "Login should fail with incorrect password.");
    }
}
