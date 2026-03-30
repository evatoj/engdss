# Sistema de Pagamentos PIX — Arquitetura de Microsserviços

## Visão Geral

O sistema é composto por dois microsserviços que se comunicam de forma assíncrona via **RabbitMQ**, seguindo os padrões **CQRS**, **Event Sourcing** e **Transactional Outbox**.

```
┌─────────────────┐     RabbitMQ      ┌─────────────────┐
│ transaction-    │ ──────────────→   │ ledger-service  │
│ service         │ ←──────────────   │ (porta 8082)    │
│ (porta 8080)    │                   │                 │
└─────────────────┘                   └─────────────────┘
        │                                      │
        ▼                                      ▼
   PostgreSQL                            PostgreSQL
 (schema: transaction)               (schema: ledger)
```

---

## Serviços

### transaction-service (porta 8080)
Responsável por gerenciar usuários e transações PIX. É o ponto de entrada do sistema.

### ledger-service (porta 8082)
Responsável pelo registro financeiro imutável. Mantém o histórico completo de eventos (Event Sourcing) e uma visão materializada do saldo (CQRS).

---

## Fluxo Completo

### 1. Criação de Usuário com Saldo Inicial

```
POST /usuarios
{"nome": "João", "saldoInicial": 500.00}
```

```
transaction-service
    │
    ├── Salva usuário no banco (schema: transaction)
    │
    └── Publica evento no RabbitMQ
            routing key: ledger.credito.inicial
                    │
                    ▼
            ledger-service
                    │
                    └── Cria balance_view com saldo 500.00
                        Registra evento CREDIT no ledger_events
```

### 2. Transação PIX (Saque)

```
POST /transacoes
{"usuarioId": 1, "chavePixDestino": "...", "valor": 100.00}
```

```
transaction-service
    │
    ├── Reserva saldo do usuário (disponível → pendente)
    ├── Salva transação com status PENDENTE
    │
    └── Publica evento no RabbitMQ
            routing key: ledger.saque.iniciado
                    │
                    ▼
            ledger-service
                    │
                    ├── Verifica saldo disponível
                    ├── Registra evento DEBIT_PENDING no ledger_events
                    ├── Atualiza balance_view (disponível -100, pendente +100)
                    │
                    └── Publica resposta via Outbox Pattern
                            routing key: ledger.debited
                                    │
                                    ▼
                    transaction-service
                                    │
                                    ├── Atualiza transação para CONCLUIDA
                                    └── Confirma débito pendente do usuário
```

---

## Padrões Utilizados

### Transactional Outbox
O ledger-service não publica diretamente no RabbitMQ. Em vez disso, salva os eventos na tabela `outbox_events` dentro da mesma transação do banco. Um worker (`OutboxPublisherWorker`) lê e publica esses eventos a cada 2 segundos, garantindo que nenhum evento se perca mesmo em caso de falha.

```
ledger-service
    │
    ├── [transação ACID]
    │       ├── Salva ledger_event
    │       ├── Atualiza balance_view
    │       └── Salva outbox_event (published=false)
    │
    └── OutboxPublisherWorker (a cada 2s)
            └── Lê outbox_events não publicados
                Publica no RabbitMQ
                Marca como published=true
```

### CQRS (Command Query Responsibility Segregation)
- **Command**: `LedgerCommandService` — processa débitos, créditos e reversões
- **Query**: `LedgerQueryService` — consulta saldo e extrato sem tocar nos eventos

### Event Sourcing
Todos os eventos financeiros são imutáveis e armazenados na tabela `ledger_events`. O saldo é sempre derivado desses eventos.

---

## Filas RabbitMQ

| Fila | Publicador | Consumidor | Descrição |
|------|-----------|------------|-----------|
| `ledger.credito.inicial` | transaction-service | ledger-service | Crédito inicial ao criar usuário |
| `ledger.saque.iniciado` | transaction-service | ledger-service | Início de um saque PIX |
| `ledger.pix.confirmado` | transaction-service | ledger-service | PIX confirmado pelo banco |
| `ledger.pix.falhou` | transaction-service | ledger-service | PIX falhou — aciona reversão |
| `ledger.debited` | ledger-service | transaction-service | Confirma que o débito foi registrado |
| `ledger.debit.confirmed` | ledger-service | transaction-service | Débito final confirmado |
| `ledger.reversed` | ledger-service | transaction-service | Reversão aplicada — PIX falhou |

---

## Endpoints

### transaction-service (http://localhost:8080)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/usuarios` | Criar usuário com saldo inicial |
| GET | `/usuarios` | Listar todos os usuários |
| GET | `/usuarios/{id}/saldo` | Consultar saldo do usuário |
| GET | `/usuarios/{id}/transacoes` | Listar transações do usuário |
| POST | `/transacoes` | Criar transação PIX |
| GET | `/transacoes/{id}` | Buscar transação por ID |

### ledger-service (http://localhost:8082)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/ledger/queries/balance/{accountId}` | Saldo disponível e pendente |
| GET | `/ledger/queries/statement/{accountId}` | Extrato completo de eventos |

---

## Como Rodar

### Pré-requisitos
- Docker
- Docker Compose

### Subir o sistema
```bash
docker-compose up --build
```

### Limpar e reiniciar do zero
```bash
docker-compose down -v
docker-compose up --build
```

### Monitorar logs
```bash
docker-compose logs -f ledger-service
docker-compose logs -f transaction-service
```

### Painel do RabbitMQ
Acesse http://localhost:15672 com usuário `guest` e senha `guest` para visualizar as filas e mensagens em tempo real.

---

## Exemplo de Teste Completo

```powershell
# 1. Criar usuário
Invoke-WebRequest -Uri "http://localhost:8080/usuarios" -Method POST `
  -ContentType "application/json" `
  -Body '{"nome": "João", "saldoInicial": 500.00}' `
  -UseBasicParsing | Select-Object -ExpandProperty Content

# 2. Criar transação PIX (substituir ID pelo retornado acima)
Invoke-WebRequest -Uri "http://localhost:8080/transacoes" -Method POST `
  -ContentType "application/json" `
  -Body '{"usuarioId": 1, "chavePixDestino": "destino@pix.com", "valor": 100.00, "descricao": "Teste"}' `
  -UseBasicParsing | Select-Object -ExpandProperty Content

# 3. Verificar status da transação
Invoke-WebRequest -Uri "http://localhost:8080/transacoes/1" `
  -UseBasicParsing | Select-Object -ExpandProperty Content

# 4. Pegar accountId nos logs
docker-compose logs ledger-service | Select-String "CREDIT_INITIAL"

# 5. Consultar saldo no ledger (substituir UUID pelo accountId)
Invoke-WebRequest -Uri "http://localhost:8082/ledger/queries/balance/SEU-UUID" `
  -UseBasicParsing | Select-Object -ExpandProperty Content

# 6. Consultar extrato no ledger
Invoke-WebRequest -Uri "http://localhost:8082/ledger/queries/statement/SEU-UUID" `
  -UseBasicParsing | Select-Object -ExpandProperty Content
```