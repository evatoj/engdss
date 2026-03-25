USE transaction_db;

CREATE TABLE transaction_request (
    id CHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    client_id VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE outbox_event (
    id CHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL
);