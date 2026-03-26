USE ledger_db;

INSERT INTO account (id, account_number, owner_name, balance)
VALUES ('1', '12345', 'Joao', 1000.00);

USE transaction_db;

INSERT INTO transaction_request (
    id, transaction_id, client_id, amount, status
) VALUES (
    '2', 'tx-001', '12345', 100.00, 'RECEIVED'
);

INSERT INTO outbox_event (
    id, transaction_id, event_type, payload, routing_key
) VALUES (
    '3',
    'tx-001',
    'WITHDRAW_REQUEST',
    '{"amount":100}',
    'withdraw.request'
);