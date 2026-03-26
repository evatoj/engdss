-- =========================================
-- BANCO
-- =========================================
-- CREATE DATABASE ledger_db;

-- =========================================
-- TABELA: usuario
-- =========================================
CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    saldo_disponivel NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    saldo_pendente NUMERIC(15,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX IF NOT EXISTS idx_usuario_nome
    ON usuario(nome);

-- =========================================
-- TABELA: transacao_pix
-- =========================================
CREATE TABLE IF NOT EXISTS transacao_pix (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    chave_pix_destino VARCHAR(255) NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    descricao VARCHAR(255),
    tipo_operacao VARCHAR(30) NOT NULL DEFAULT 'SAQUE',
    status VARCHAR(30) NOT NULL,
    chave_idempotencia VARCHAR(100) UNIQUE,
    referencia_externa VARCHAR(100),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transacao_pix_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_usuario_id
    ON transacao_pix(usuario_id);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_status
    ON transacao_pix(status);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_data_criacao
    ON transacao_pix(data_criacao);

CREATE INDEX IF NOT EXISTS idx_transacao_pix_referencia_externa
    ON transacao_pix(referencia_externa);

-- =========================================
-- TABELA: ledger
-- =========================================
CREATE TABLE IF NOT EXISTS ledger (
    id BIGSERIAL PRIMARY KEY,
    transacao_pix_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_lancamento VARCHAR(20) NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_transacao_pix
        FOREIGN KEY (transacao_pix_id)
        REFERENCES transacao_pix(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ledger_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_ledger_usuario_id
    ON ledger(usuario_id);

CREATE INDEX IF NOT EXISTS idx_ledger_transacao_pix_id
    ON ledger(transacao_pix_id);

CREATE INDEX IF NOT EXISTS idx_ledger_data_criacao
    ON ledger(data_criacao);

-- =========================================
-- TABELA: evento_outbox
-- =========================================
CREATE TABLE IF NOT EXISTS evento_outbox (
    id BIGSERIAL PRIMARY KEY,
    transacao_pix_id BIGINT,
    tipo_evento VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    tentativas INTEGER NOT NULL DEFAULT 0,
    erro_ultimo_envio TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_publicacao TIMESTAMP NULL,

    CONSTRAINT fk_outbox_transacao_pix
        FOREIGN KEY (transacao_pix_id)
        REFERENCES transacao_pix(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_status
    ON evento_outbox(status);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_data_criacao
    ON evento_outbox(data_criacao);

CREATE INDEX IF NOT EXISTS idx_evento_outbox_transacao_pix_id
    ON evento_outbox(transacao_pix_id);

-- =========================================
-- TABELA: idempotencia
-- =========================================
CREATE TABLE IF NOT EXISTS idempotencia (
    id BIGSERIAL PRIMARY KEY,
    chave VARCHAR(100) NOT NULL UNIQUE,
    transacao_pix_id BIGINT,
    requisicao_hash VARCHAR(255),
    resposta JSONB,
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSANDO',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_idempotencia_transacao_pix
        FOREIGN KEY (transacao_pix_id)
        REFERENCES transacao_pix(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_idempotencia_data_criacao
    ON idempotencia(data_criacao);

CREATE INDEX IF NOT EXISTS idx_idempotencia_transacao_pix_id
    ON idempotencia(transacao_pix_id);