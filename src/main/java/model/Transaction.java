package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private int txnId;
    private int fromAccount;
    private int toAccount;
    private BigDecimal amount;
    private String txnType;
    private String status;
    private Timestamp timestamp;

    public Transaction(int txnId, int fromAccount, int toAccount, BigDecimal amount, String txnType, String status, Timestamp timestamp) {
        this.txnId = txnId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.txnType = txnType;
        this.status = status;
        this.timestamp = timestamp;
    }

    public int getTxnId() { return txnId; }
    public void setTxnId(int txnId) { this.txnId = txnId; }
    public int getFromAccount() { return fromAccount; }
    public void setFromAccount(int fromAccount) { this.fromAccount = fromAccount; }
    public int getToAccount() { return toAccount; }
    public void setToAccount(int toAccount) { this.toAccount = toAccount; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
