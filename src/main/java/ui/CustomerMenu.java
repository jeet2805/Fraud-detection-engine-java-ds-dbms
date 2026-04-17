package ui;

import service.TransactionService;
import model.Account;
import model.Transaction;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private final Scanner scanner;
    private final TransactionService txnService;

    public CustomerMenu() {
        this.scanner = new Scanner(System.in);
        this.txnService = new TransactionService();
    }

    public void display() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n=== Customer Dashboard ===");
            System.out.println("Welcome, " + SessionManager.getCurrentUser().getUsername());
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Transfer Funds");
            System.out.println("4. View Statement");
            System.out.println("5. View My Alerts");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            int userId = SessionManager.getCurrentUser().getUserId();

            try {
                Account account = txnService.getAccountByUserId(userId);
                if (account == null && !choice.equals("6")) {
                    System.out.println("No account found for user.");
                    continue;
                }

                switch (choice) {
                    case "1":
                        System.out.println("Current Balance: $" + account.getBalance());
                        System.out.println("Account Status: " + account.getStatus());
                        break;
                    case "2":
                        System.out.print("Enter amount to deposit: ");
                        BigDecimal amount = new BigDecimal(scanner.nextLine());
                        if (txnService.deposit(account.getAccountId(), amount)) {
                            System.out.println("Deposit successful!");
                        } else {
                            System.out.println("Deposit failed. Check amount.");
                        }
                        break;
                    case "3":
                        if ("FROZEN".equals(account.getStatus())) {
                            System.out.println("Your account is frozen. Transfers are blocked.");
                            break;
                        }
                        System.out.print("Enter recipient ACCOUNT ID (not User ID): ");
                        int toAccountId = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter amount to transfer: ");
                        BigDecimal transferAmount = new BigDecimal(scanner.nextLine());
                        if (txnService.transfer(userId, toAccountId, transferAmount)) {
                            System.out.println("Transfer successful!");
                            // Re-fetch account to show updated balance in the next loop or immediate refresh
                            account = txnService.getAccountByUserId(userId);
                        } else {
                            System.out.println("Transfer failed. Check balance or account ID.");
                        }
                        break;
                    case "4":
                        System.out.println("--- Recent Transactions ---");
                        List<Transaction> txns = txnService.getStatement(account.getAccountId());
                        for (Transaction t : txns) {
                            String dir = (t.getFromAccount() == account.getAccountId()) ? "OUT" : "IN ";
                            System.out.printf("[%s] %s | %s $%.2f | Status: %s%n",
                                t.getTimestamp(), dir, t.getTxnType(), t.getAmount(), t.getStatus());
                        }
                        break;
                    case "5":
                        System.out.println("--- Your Security Alerts ---");
                        List<String> alerts = txnService.getAlerts(account.getAccountId());
                        if (alerts.isEmpty()) {
                            System.out.println("No security alerts. Your account is safe.");
                        } else {
                            for (String alert : alerts) {
                                System.out.println(alert);
                            }
                        }
                        break;
                    case "6":
                        SessionManager.logout();
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format entered.");
            }
        }
    }
}
