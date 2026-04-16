package ui;

public class AnalystMenu {
    public void display() {
        System.out.println("=== Analyst Dashboard ===");
        System.out.println("Welcome, " + SessionManager.getCurrentUser().getUsername());
        System.out.println("1. Logout");
        // Stub for now
    }
}
