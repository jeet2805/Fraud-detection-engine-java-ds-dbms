package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private int accountId;
    private int userId;
    private String holderName;
    private BigDecimal balance;
    private String status;
    private Timestamp createdAt;

    public Account(int accountId, int userId, String holderName, BigDecimal balance, String status, Timestamp createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
