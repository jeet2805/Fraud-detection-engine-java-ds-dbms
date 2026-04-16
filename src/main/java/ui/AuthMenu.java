package ui;

import model.User;
import service.AuthService;
import java.util.Scanner;

public class AuthMenu {
    private final AuthService authService;
    private final Scanner scanner;

    public AuthMenu() {
        this.authService = new AuthService();
        this.scanner = new Scanner(System.in);
    }

    public void display() {
        while (true) {
            System.out.println("\n=== Fraud Detection Engine ===");
            System.out.println("1. Login");
            System.out.println("2. Signup");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleSignup();
                    break;
                case "3":
                    System.out.println("Exiting the application. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = authService.login(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                System.out.println("Login successful! Role: " + user.getRole());
                routeToDashboard();
            } else {
                System.out.println("Login failed. Invalid username or password.");
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private void handleSignup() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Role (CUSTOMER/ANALYST/ADMIN): ");
        String role = scanner.nextLine().toUpperCase();

        if (!role.equals("CUSTOMER") && !role.equals("ANALYST") && !role.equals("ADMIN")) {
            System.out.println("Invalid role selected. Must be CUSTOMER, ANALYST, or ADMIN.");
            return;
        }

        boolean success = authService.signup(username, password, role);
        if (success) {
            System.out.println("Signup successful. You can now log in.");
        } else {
            System.out.println("Signup failed. Username may already exist.");
        }
    }

    private void routeToDashboard() {
        User user = SessionManager.getCurrentUser();
        while (SessionManager.getCurrentUser() != null) {
            switch (user.getRole()) {
                case "CUSTOMER":
                    new CustomerMenu().display();
                    // Auto logout after stub displays to avoid infinite loops for now
                    SessionManager.logout();
                    break;
                case "ANALYST":
                    new AnalystMenu().display();
                    SessionManager.logout();
                    break;
                case "ADMIN":
                    new AdminMenu().display();
                    SessionManager.logout();
                    break;
                default:
                    System.out.println("Unknown role.");
                    SessionManager.logout();
            }
        }
    }
}
