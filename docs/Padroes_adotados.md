# Padrões Arquiteturais Aplicados

## POC 3 — Ledger + PIX Sandbox com Idempotência

---

## Visão Geral

Para justificar as decisões arquiteturais do projeto, mapeamos os padrões aplicados a partir das seguintes perguntas centrais do escopo:

| Pergunta | Padrão |
|---|---|
| Como o estado do ledger é persistido? | Event Sourcing |
| Como as garantias financeiras são mantidas? | ACID |
| Como o fluxo de saque multi-passo é coordenado e o que acontece quando o PIX falha? | SAGA Pattern |
| Como o retry não duplica o saque? | Idempotência |
| Por que a arquitetura é assíncrona? | EDA |
| Como queries de saldo são viáveis sem agregar todos os eventos? | CQRS |

---

## Áreas Técnicas Cobertas

| Área | Padrões |
|---|---|
| Confiabilidade | ACID, Event Sourcing, SAGA, Idempotência, EDA |
| Desempenho | CQRS |

> O projeto cobre 2 das 5 áreas técnicas exigidas, atendendo ao requisito mínimo do documento de definição.

---

## Padrões Aplicados

### 1. Event Sourcing — Confiabilidade

**Problema:** Precisamos rastrear todas as operações que ocorreram para que o saldo atual de um usuário seja X. Sem esse histórico, não é possível auditar entradas e saídas, nem identificar em qual etapa um fluxo multi-passo falhou.

**Decisão:** Cada mudança de estado no ledger gera um evento imutável. O saldo é derivado da sequência de eventos. Nenhum lançamento é editado — apenas estornado via novo evento.

**Consequências:**
- Rastreabilidade completa do fluxo financeiro
- Auditoria imutável por design
- Identificação precisa de falhas em fluxos multi-passo
- Query de saldo requer agregação dos eventos, mitigado via CQRS

---

### 2. ACID — Confiabilidade

**Problema:** Operações financeiras precisam de garantias de que o dinheiro não some, não duplica e o estado do banco permanece consistente mesmo em caso de falha.

**Decisão:** Toda operação no ledger é executada dentro de uma transação ACID, garantindo Atomicidade, Consistência, Isolamento e Durabilidade.

**Consequências:**
- Débito e crédito ocorrem juntos ou nenhum ocorre
- Saldo nunca fica em estado inválido
- Transação confirmada persiste mesmo em caso de queda do servidor
- Base obrigatória para o funcionamento do Transactional Outbox

---

### 3. SAGA Pattern — Confiabilidade

**Problema:** O fluxo de saque envolve múltiplos passos independentes — débito no ledger, envio do PIX, confirmação e cobrança de fee — que não podem ser executados em uma única transação ACID distribuída.

**Decisão:** Adotamos o SAGA Pattern com coreografia. Cada passo do fluxo é um evento, e cada passo possui uma ação compensatória em caso de falha.

**Fluxo:**
```
SaqueIniciado       → Ledger debita (pending)
PIXEnviado          → PIX API processa
PIXConfirmado       → Ledger confirma + cobra fee
PIXFalhou           → Ledger estorna débito (compensação)
```

**Consequências:**
- Fluxo multi-passo coordenado com compensações explícitas
- Não existe rollback distribuído — apenas ações compensatórias
- Maior rastreabilidade de cada etapa do fluxo

---

### 4. Idempotência — Confiabilidade

**Problema:** Em um fluxo assíncrono, o cliente pode reenviar a mesma requisição em caso de timeout, duplicando o saque.

**Decisão:** Toda requisição de saque carrega uma `Idempotency-Key` gerada pelo cliente. O sistema armazena a key com o resultado. Requisições com a mesma key retornam o resultado anterior sem reprocessar.

**Fluxo:**
```
POST /saques (Idempotency-Key: uuid)
  → key já existe? retorna resultado anterior
  → key nova? processa e persiste resultado
```

**Consequências:**
- Retry de saque não duplica crédito
- Cobre o teste explícito de idempotência do escopo
- Key armazenada com TTL configurável

---

### 5. EDA (Event-Driven Architecture) — Confiabilidade

**Problema:** O protocolo PIX do Banco Central não retorna a confirmação de pagamento de forma síncrona. Ele retorna `202 Accepted` e notifica o resultado via webhook, impedindo um fluxo de request/response tradicional.

**Decisão:** Adotamos EDA como padrão arquitetural central. Cada etapa do fluxo de saque é disparada e processada por eventos, com workers independentes responsáveis por cada passo.

**Consequências:**
- Respostas rápidas ao cliente — a API retorna 202 imediatamente
- Processamento concorrente via workers independentes
- Alinhamento natural com o protocolo PIX, que já é assíncrono por design
- Maior complexidade operacional — exige observabilidade e rastreamento de eventos

---

### 6. CQRS (Command Query Responsibility Segregation) — Desempenho

**Problema:** Com Event Sourcing, toda query de saldo requer agregação de todos os eventos da conta, o que se torna inviável em produção.

**Decisão:** Separamos o modelo de escrita do modelo de leitura. Escritas geram eventos no ledger. Leituras consomem views materializadas com o saldo já calculado.

**Fluxo:**
```
Escrita (Command): POST /saques → gera eventos no ledger
Leitura (Query):   GET /saldo   → lê view materializada
                   GET /extrato → lê eventos diretamente
```

**Consequências:**
- Queries de saldo eficientes sem agregar todos os eventos
- Separação clara entre fluxo de escrita e leitura
- View materializada precisa ser atualizada a cada novo evento

---

## Mecanismos de Implementação

Os seguintes padrões estão presentes no projeto como mecanismos de implementação, mas não são padrões arquiteturais centrais:

| Mecanismo | Função |
|---|---|
| Transactional Outbox | Garante entrega do evento para a fila dentro da mesma transação ACID |
| Retry Pattern + DLQ | Reprocessamento de eventos falhos com backoff exponencial |
| Strong Consistency | Leituras de saldo sempre no nó primário, sem defasagem |
| Quarentena Financeira | PIX recebido em pending_balance com liberação configurável |