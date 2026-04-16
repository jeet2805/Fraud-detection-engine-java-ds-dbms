package ui;

import model.User;

public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        SessionManager.currentUser = currentUser;
    }

    public static void logout() {
        currentUser = null;
        System.out.println("Logged out successfully.");
    }
}
