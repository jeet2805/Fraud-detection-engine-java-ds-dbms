-- ============================================================================
-- Fraud Detection Engine — PostgreSQL Schema
-- ============================================================================

-- Table 1: Users
CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,  -- CUSTOMER / ANALYST / ADMIN
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMP    DEFAULT NOW()
);

-- Table 2: Accounts
CREATE TABLE accounts (
    account_id   SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(user_id),
    holder_name  VARCHAR(100) NOT NULL,
    balance      DECIMAL(15,2) DEFAULT 0.00,
    status       VARCHAR(20)   DEFAULT 'ACTIVE',  -- ACTIVE / FROZEN
    created_at   TIMESTAMP     DEFAULT NOW()
);

-- Table 3: Transactions
CREATE TABLE transactions (
    txn_id        SERIAL PRIMARY KEY,
    from_account  INT REFERENCES accounts(account_id),
    to_account    INT REFERENCES accounts(account_id),
    amount        DECIMAL(15,2) NOT NULL,
    txn_type      VARCHAR(20),  -- TRANSFER / DEPOSIT / WITHDRAWAL
    status        VARCHAR(20),  -- SUCCESS / FLAGGED / ROLLED_BACK
    timestamp     TIMESTAMP DEFAULT NOW()
);

-- Table 4: Fraud Alerts
CREATE TABLE fraud_alerts (
    alert_id       SERIAL PRIMARY KEY,
    txn_id         INT REFERENCES transactions(txn_id),
    rule_triggered VARCHAR(100),  -- CYCLE_DETECTED, HIGH_VELOCITY, etc
    risk_score     INT,           -- 0 to 100
    reviewed       BOOLEAN DEFAULT FALSE,
    resolution     VARCHAR(20),   -- APPROVED / REJECTED
    created_at     TIMESTAMP DEFAULT NOW()
);

-- Table 5: Audit Log
CREATE TABLE audit_log (
    log_id       SERIAL PRIMARY KEY,
    action       VARCHAR(50),   -- COMMIT / ROLLBACK / FREEZE / LOGIN_FAIL
    entity_type  VARCHAR(30),   -- TRANSACTION / ACCOUNT / USER
    entity_id    INT,
    performed_by INT REFERENCES users(user_id),
    reason       TEXT,
    logged_at    TIMESTAMP DEFAULT NOW()
);

-- Table 6: Rule Config
CREATE TABLE rule_config (
    rule_name   VARCHAR(50) PRIMARY KEY,
    threshold   DECIMAL(15,2),
    is_active   BOOLEAN DEFAULT TRUE
);

-- Seed rule_config
INSERT INTO rule_config VALUES ('LARGE_AMOUNT_THRESHOLD', 50000.00, true);
INSERT INTO rule_config VALUES ('VELOCITY_MAX_TXN',       5,        true);
INSERT INTO rule_config VALUES ('VELOCITY_WINDOW_SEC',    60,       true);
INSERT INTO rule_config VALUES ('NEW_ACCOUNT_HOURS',      24,       true);

-- Table 7: Login Log
CREATE TABLE login_log (
    log_id     SERIAL PRIMARY KEY,
    user_id    INT REFERENCES users(user_id),
    status     VARCHAR(20),  -- SUCCESS / FAILED
    login_time TIMESTAMP DEFAULT NOW()
);

-- ============================================================================
-- Views
-- ============================================================================

-- View 1: Flagged transactions for analyst dashboard
CREATE VIEW flagged_transactions_view AS
SELECT fa.alert_id, fa.risk_score, fa.rule_triggered,
       t.amount, t.from_account, t.to_account, t.timestamp,
       fa.reviewed, fa.resolution
FROM fraud_alerts fa
JOIN transactions t ON fa.txn_id = t.txn_id
WHERE fa.reviewed = FALSE
ORDER BY fa.risk_score DESC;

-- View 2: Daily transaction volume for admin dashboard
CREATE VIEW daily_volume_view AS
SELECT DATE(timestamp) as txn_date,
       COUNT(*) as total_txns,
       SUM(amount) as total_volume,
       COUNT(CASE WHEN status='FLAGGED' THEN 1 END) as flagged_count
FROM transactions
GROUP BY DATE(timestamp)
ORDER BY txn_date DESC;

-- ============================================================================
-- Trigger
-- ============================================================================

-- Auto-freeze account on 3+ unreviewed alerts
CREATE OR REPLACE FUNCTION auto_freeze_account() RETURNS TRIGGER AS $$
DECLARE alert_count INT;
BEGIN
    SELECT COUNT(*) INTO alert_count FROM fraud_alerts fa
    JOIN transactions t ON fa.txn_id = t.txn_id
    WHERE t.from_account = (SELECT from_account FROM transactions WHERE txn_id = NEW.txn_id)
    AND fa.reviewed = FALSE;
    
    IF alert_count >= 3 THEN
        UPDATE accounts SET status = 'FROZEN'
        WHERE account_id = (SELECT from_account FROM transactions WHERE txn_id = NEW.txn_id);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER freeze_on_alerts AFTER INSERT ON fraud_alerts FOR EACH ROW EXECUTE FUNCTION auto_freeze_account();
