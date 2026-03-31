INSERT INTO transaction.usuario (id, nome)
VALUES ('11111111-1111-1111-1111-111111111111', 'João')
ON CONFLICT (id) DO NOTHING;

INSERT INTO transaction.usuario (id, nome)
VALUES ('22222222-2222-2222-2222-222222222222', 'Maria')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ledger.balance_view (
    account_id,
    available_balance,
    pending_balance,
    updated_at,
    version
)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    1000.00,
    0.00,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO ledger.balance_view (
    account_id,
    available_balance,
    pending_balance,
    updated_at,
    version
)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    500.00,
    0.00,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO ledger.ledger_events (
    id,
    account_id,
    idempotency_key,
    type,
    amount,
    occurred_at,
    correlation_id
)
VALUES (
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    gen_random_uuid(),
    'CREDIT',
    1000.00,
    CURRENT_TIMESTAMP,
    gen_random_uuid()
);

INSERT INTO ledger.ledger_events (
    id,
    account_id,
    idempotency_key,
    type,
    amount,
    occurred_at,
    correlation_id
)
VALUES (
    gen_random_uuid(),
    '22222222-2222-2222-2222-222222222222',
    gen_random_uuid(),
    'CREDIT',
    500.00,
    CURRENT_TIMESTAMP,
    gen_random_uuid()
);