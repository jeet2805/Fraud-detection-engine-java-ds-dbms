package ui;

public class CustomerMenu {
    public void display() {
        System.out.println("=== Customer Dashboard ===");
        System.out.println("Welcome, " + SessionManager.getCurrentUser().getUsername());
        System.out.println("1. Logout");
        // Stub for now
    }
}
