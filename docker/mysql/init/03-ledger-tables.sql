USE ledger_db;

CREATE TABLE account (
    id CHAR(36) PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    owner_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ledger_entry (
    id CHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    account_id CHAR(36) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id)
);