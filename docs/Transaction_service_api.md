## API da POC Ledger + PIX

Base URL de exemplo:

```text
http://localhost:8080
```

---

## 1. Criar usuário

Cria um usuário no `transaction-service` e solicita o crédito inicial no ledger.

### Endpoint

```http
POST /usuarios
```

### Body

```json
{
  "nome": "Cirilo",
  "saldoInicial": 100000.00
}
```

### Exemplo com curl

```bash
curl -X POST "http://localhost:8080/usuarios" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Cirilo",
    "saldoInicial": 100000.00
  }'
```

### Resposta esperada

```json
{
  "id": "75b6e354-797b-4d64-95b3-7f1f7736e74b",
  "nome": "Cirilo",
  "saldo": 100000.00
}
```

---

## 2. Listar usuários

Lista os usuários e consulta o saldo oficial no ledger.

### Endpoint

```http
GET /usuarios
```

### Exemplo com curl

```bash
curl "http://localhost:8080/usuarios"
```

### Resposta esperada

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "nome": "João",
    "saldo": 1000.0000
  },
  {
    "id": "22222222-2222-2222-2222-222222222222",
    "nome": "Maria",
    "saldo": 500.0000
  },
  {
    "id": "75b6e354-797b-4d64-95b3-7f1f7736e74b",
    "nome": "Cirilo",
    "saldo": 100000.0000
  }
]
```

---

## 3. Consultar saldo de um usuário

Consulta o saldo oficial do usuário no ledger.

### Endpoint

```http
GET /usuarios/{id}/saldo
```

### Exemplo com curl

```bash
curl "http://localhost:8080/usuarios/75b6e354-797b-4d64-95b3-7f1f7736e74b/saldo"
```

### Resposta esperada

```json
{
  "usuarioId": "75b6e354-797b-4d64-95b3-7f1f7736e74b",
  "nome": "Cirilo",
  "saldo": 100000.0000
}
```

---

## 4. Listar transações de um usuário

Retorna o histórico de transações PIX do usuário.

### Endpoint

```http
GET /usuarios/{id}/transacoes
```

### Exemplo com curl

```bash
curl "http://localhost:8080/usuarios/11111111-1111-1111-1111-111111111111/transacoes"
```

### Resposta esperada

```json
[
  {
    "id": "0dfe7b9c-64b5-4b7f-a507-cc5ec2d7b3f6",
    "usuarioId": "11111111-1111-1111-1111-111111111111",
    "chavePixDestino": "teste@pix.com",
    "valor": 100.00,
    "descricao": "Teste PIX",
    "status": "EM_PROCESSAMENTO",
    "dataCriacao": "2026-03-31T20:30:15.123"
  }
]
```

---

## 5. Criar transação PIX

Cria uma solicitação de transação PIX para um usuário.

### Endpoint

```http
POST /transacoes
```

### Headers

```http
Content-Type: application/json
Idempotency-Key: 33333333-3333-3333-3333-333333333333
```

### Body

```json
{
  "usuarioId": "11111111-1111-1111-1111-111111111111",
  "chavePixDestino": "teste@pix.com",
  "valor": 100.00,
  "descricao": "Teste PIX"
}
```

### Exemplo com curl

```bash
curl -X POST "http://localhost:8080/transacoes" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 33333333-3333-3333-3333-333333333333" \
  -d '{
    "usuarioId": "11111111-1111-1111-1111-111111111111",
    "chavePixDestino": "teste@pix.com",
    "valor": 100.00,
    "descricao": "Teste PIX"
  }'
```

### Resposta esperada

```json
{
  "id": "0dfe7b9c-64b5-4b7f-a507-cc5ec2d7b3f6",
  "usuarioId": "11111111-1111-1111-1111-111111111111",
  "chavePixDestino": "teste@pix.com",
  "valor": 100.00,
  "descricao": "Teste PIX",
  "status": "PENDENTE",
  "dataCriacao": "2026-03-31T20:30:15.123"
}
```

---

## 6. Consultar transação por ID

Busca os dados de uma transação PIX específica.

### Endpoint

```http
GET /transacoes/{id}
```

### Exemplo com curl

```bash
curl "http://localhost:8080/transacoes/0dfe7b9c-64b5-4b7f-a507-cc5ec2d7b3f6"
```

### Resposta esperada

```json
{
  "id": "0dfe7b9c-64b5-4b7f-a507-cc5ec2d7b3f6",
  "usuarioId": "11111111-1111-1111-1111-111111111111",
  "chavePixDestino": "teste@pix.com",
  "valor": 100.00,
  "descricao": "Teste PIX",
  "status": "EM_PROCESSAMENTO",
  "dataCriacao": "2026-03-31T20:30:15.123"
}
```

---

## Status possíveis da transação

Exemplos de status usados no fluxo atual:

* `PENDENTE`
* `EM_PROCESSAMENTO`
* `CONCLUIDA`
* `FALHA`

---

## Observação sobre o fluxo assíncrono

A criação de usuário com saldo inicial e a criação de transação PIX envolvem processamento assíncrono via RabbitMQ. Então pode acontecer:

* o `POST /usuarios` responder antes do ledger consolidar o saldo
* o `POST /transacoes` responder com `PENDENTE`
* depois o status evoluir para `EM_PROCESSAMENTO`, `CONCLUIDA` ou `FALHA`

Por isso, após criar uma transação, o ideal é consultar:

* `GET /transacoes/{id}`
* ou `GET /usuarios/{id}/transacoes`

Se quiser, eu também posso transformar isso em um `README.md` já formatado para colar no projeto.
