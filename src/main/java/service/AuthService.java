package service;

import dao.AccountDAO;
import dao.UserDAO;
import db.DBConnection;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            if (!user.isActive()) {
                System.out.println("Account is disabled.");
                userDAO.logLogin(user.getUserId(), "FAILED_DISABLED");
                return null;
            }
            userDAO.logLogin(user.getUserId(), "SUCCESS");
            return user;
        }
        if (user != null) {
            userDAO.logLogin(user.getUserId(), "FAILED");
        } else {
            userDAO.logLogin(-1, "FAILED");
        }
        return null;
    }

    public boolean signup(String username, String password, String role) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // ACID transaction

            // Check if user exists
            if (userDAO.findByUsername(username) != null) {
                return false;
            }

            User user = new User(username, hashPassword(password), role);
            int userId = userDAO.insertUser(user, conn);

            // If customer, create an account
            if ("CUSTOMER".equals(role)) {
                accountDAO.insertAccount(userId, username + " Account", conn);
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
