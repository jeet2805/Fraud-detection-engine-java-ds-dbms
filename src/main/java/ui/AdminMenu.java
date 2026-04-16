package ui;

public class AdminMenu {
    public void display() {
        System.out.println("=== Admin Dashboard ===");
        System.out.println("Welcome, " + SessionManager.getCurrentUser().getUsername());
        System.out.println("1. Logout");
        // Stub for now
    }
}
