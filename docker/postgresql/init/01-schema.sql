-- =========================================
-- SCHEMAS
-- =========================================
CREATE SCHEMA IF NOT EXISTS transaction;
CREATE SCHEMA IF NOT EXISTS ledger;

-- =========================================
-- SCHEMA: transaction
-- =========================================

-- TABELA: usuario
CREATE TABLE IF NOT EXISTS transaction.usuario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100)   NOT NULL,
    saldo_disponivel NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    saldo_pendente   NUMERIC(15,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX IF NOT EXISTS idx_usuario_nome
    ON transaction.usuario(nome);

-- TABELA: transacao_pix
CREATE TABLE IF NOT EXISTS transaction.transacao_pix (
    id                   BIGSERIAL PRIMARY KEY,
    usuario_id           BIGINT        NOT NULL,
    chave_pix_destino    VARCHAR(255)  NOT NULL,
    valor                NUMERIC(15,2) NOT NULL,
    descricao            VARCHAR(255),
    tipo_operacao        VARCHAR(30)   NOT NULL DEFAULT 'SAQUE',
    status               VARCHAR(30)   NOT NULL,
    chave_idempotencia   VARCHAR(100)  UNIQUE,
    referencia_externa   VARCHAR(100),
    data_criacao         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transacao_pix_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES transaction.usuario(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_usuario_id
    ON transaction.transacao_pix(usuario_id);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_status
    ON transaction.transacao_pix(status);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_data_criacao
    ON transaction.transacao_pix(data_criacao);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_referencia_externa
    ON transaction.transacao_pix(referencia_externa);

-- TABELA: evento_outbox
CREATE TABLE IF NOT EXISTS transaction.evento_outbox (
    id                BIGSERIAL PRIMARY KEY,
    transacao_pix_id  BIGINT        NULL,
    tipo_evento       VARCHAR(100)  NOT NULL,
    payload           JSONB         NOT NULL,
    routing_key       VARCHAR(100)  NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    tentativas        INTEGER       NOT NULL DEFAULT 0,
    erro_ultimo_envio TEXT,
    data_criacao      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_publicacao   TIMESTAMP     NULL,

    CONSTRAINT fk_outbox_transacao_pix
        FOREIGN KEY (transacao_pix_id)
        REFERENCES transaction.transacao_pix(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_status
    ON transaction.evento_outbox(status);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_data_criacao
    ON transaction.evento_outbox(data_criacao);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_transacao_pix_id
    ON transaction.evento_outbox(transacao_pix_id);

-- TABELA: idempotencia
CREATE TABLE IF NOT EXISTS transaction.idempotencia (
    id               BIGSERIAL PRIMARY KEY,
    chave            VARCHAR(100)  NOT NULL UNIQUE,
    transacao_pix_id BIGINT        NULL,
    requisicao_hash  VARCHAR(255),
    resposta         JSONB,
    status           VARCHAR(30)   NOT NULL DEFAULT 'PROCESSANDO',
    data_criacao     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_idempotencia_transacao_pix
        FOREIGN KEY (transacao_pix_id)
        REFERENCES transaction.transacao_pix(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_idempotencia_data_criacao
    ON transaction.idempotencia(data_criacao);

CREATE INDEX IF NOT EXISTS idx_idempotencia_transacao_pix_id
    ON transaction.idempotencia(transacao_pix_id);

-- =========================================
-- SCHEMA: ledger
-- =========================================

-- TABELA: ledger_events (Event Sourcing)
CREATE TABLE IF NOT EXISTS ledger.ledger_events (
    id              UUID PRIMARY KEY,
    account_id      UUID          NOT NULL,
    idempotency_key UUID          NOT NULL,
    type            VARCHAR(30)   NOT NULL,
    amount          NUMERIC(19,4) NOT NULL,
    occurred_at     TIMESTAMP     NOT NULL,
    correlation_id  UUID
);

CREATE INDEX IF NOT EXISTS idx_ledger_events_account_id
    ON ledger.ledger_events(account_id);

CREATE INDEX IF NOT EXISTS idx_ledger_events_correlation_id
    ON ledger.ledger_events(correlation_id);

CREATE INDEX IF NOT EXISTS idx_ledger_events_idempotency_key
    ON ledger.ledger_events(idempotency_key);

-- TABELA: balance_view (CQRS)
CREATE TABLE IF NOT EXISTS ledger.balance_view (
    account_id        UUID          PRIMARY KEY,
    available_balance NUMERIC(19,4) NOT NULL DEFAULT 0,
    pending_balance   NUMERIC(19,4) NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP     NOT NULL,
    version           BIGINT        NOT NULL DEFAULT 0
);

-- TABELA: outbox_events (Transactional Outbox)
CREATE TABLE IF NOT EXISTS ledger.outbox_events (
    id          UUID          PRIMARY KEY,
    event_type  VARCHAR(100)  NOT NULL,
    payload     TEXT          NOT NULL,
    routing_key VARCHAR(100)  NOT NULL,
    published   BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_published
    ON ledger.outbox_events(published);

CREATE INDEX IF NOT EXISTS idx_outbox_events_created_at
    ON ledger.outbox_events(created_at);

-- =========================================
-- DADOS INICIAIS
-- =========================================
INSERT INTO transaction.usuario (nome, saldo_disponivel, saldo_pendente)
VALUES ('João', 1000.00, 0.00);

INSERT INTO transaction.usuario (nome, saldo_disponivel, saldo_pendente)
VALUES ('Maria', 500.00, 0.00);