# Integração SAGA ↔ Biometric Adapter Service

## Visão Geral

O `biometric-adapter-service` **não expõe nenhum endpoint HTTP**. Toda a comunicação com o SAGA ocorre exclusivamente via **RabbitMQ**, seguindo o mesmo padrão já utilizado pelo `ledger-service`.

---

## Fluxo Completo

```
SAGA (transaction-service)
  │
  │  publica mensagem JSON
  ▼
RabbitMQ: biometric.verification.request
  │
  │  BiometricRequestConsumer escuta
  ▼
biometric-adapter-service processa...
  │   ├── chama Validra API (ou stub APPROVED)
  │   └── mapeia resultado → BiometricResponse
  │
  │  publica resultado JSON
  ▼
RabbitMQ: biometric.verification.response
  │
  │  SAGA consome e continua o fluxo
  ▼
SAGA (transaction-service)
```

---

## Filas

| Fila                               | Direção         | Quem publica         | Quem consome              |
|------------------------------------|-----------------|----------------------|---------------------------|
| `biometric.verification.request`   | SAGA → Adapter  | transaction-service  | BiometricRequestConsumer  |
| `biometric.verification.response`  | Adapter → SAGA  | BiometricResponsePublisher | transaction-service  |
| `biometric.verification.request.dlq`  | DLQ          | RabbitMQ (automático)| Análise manual            |
| `biometric.verification.response.dlq` | DLQ         | RabbitMQ (automático)| Análise manual            |

**Exchange:** `biometric.exchange` (direct, durable)

---

## Contrato das Mensagens

### BiometricRequest — SAGA publica

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "cpf": "12345678901",
  "selfieBase64": "base64encodedimage==",
  "requestedAt": "2026-04-01T18:00:00Z",
  "attempt": 1
}
```

| Campo          | Tipo      | Obrigatório | Descrição                                      |
|----------------|-----------|-------------|------------------------------------------------|
| `transactionId`| UUID      | ✅          | ID da transação no SAGA — correlação de toda a cadeia |
| `cpf`          | String    | ✅          | CPF do portador (apenas dígitos, 11 chars)     |
| `selfieBase64` | String    | ✅          | Foto selfie em Base64 (JPEG ou PNG)            |
| `requestedAt`  | Instant   | ❌          | Momento da solicitação (UTC)                   |
| `attempt`      | int       | ❌          | Tentativa atual (default: 1)                   |

---

### BiometricResponse — SAGA consome

```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "message": "Biometric verification approved",
  "similarityScore": 0.98,
  "respondedAt": "2026-04-01T18:00:01Z"
}
```

| Campo            | Tipo    | Descrição                                                        |
|------------------|---------|------------------------------------------------------------------|
| `transactionId`  | UUID    | Mesmo ID da request — use para correlacionar no SAGA            |
| `status`         | Enum    | `APPROVED` \| `REJECTED` \| `ERROR`                             |
| `message`        | String  | Descrição do resultado ou motivo da rejeição/erro               |
| `similarityScore`| Double  | Score facial 0.0–1.0 (`null` quando status for `ERROR`)         |
| `respondedAt`    | Instant | Momento em que o adapter gerou a resposta (UTC)                 |

---

## O que o SAGA precisa implementar

### 1. Publicar a solicitação

No passo de verificação biométrica do fluxo SAGA, o `transaction-service` deve publicar:

```java
BiometricRequest request = BiometricRequest.builder()
    .transactionId(saga.getTransactionId())
    .cpf(saga.getCpf())
    .selfieBase64(saga.getSelfieBase64())
    .requestedAt(Instant.now())
    .attempt(1)
    .build();

rabbitTemplate.convertAndSend(
    "biometric.exchange",
    "biometric.verification.request",
    request
);
```

### 2. Escutar a resposta

```java
@RabbitListener(queues = "biometric.verification.response")
public void onBiometricResult(BiometricResponse response) {
    switch (response.getStatus()) {
        case APPROVED -> saga.continuar(response.getTransactionId());
        case REJECTED -> saga.compensar(response.getTransactionId(), "biometria_rejeitada");
        case ERROR    -> saga.compensar(response.getTransactionId(), "erro_provedor_biometria");
    }
}
```

> **Nota:** `BiometricRequest` e `BiometricResponse` são as mesmas classes do `biometric-adapter-service`.
> Extraia-as para um módulo compartilhado ou recriae-as no `transaction-service` com os mesmos campos.

---

## Tratamento de Erros no SAGA

| Status recebido | Significado                                                  | Ação recomendada             |
|-----------------|--------------------------------------------------------------|------------------------------|
| `APPROVED`      | Biometria validada com sucesso                               | Continuar o fluxo SAGA       |
| `REJECTED`      | Rosto não corresponde ao CPF na base Serpro                  | Compensar — negar transação  |
| `ERROR`         | Falha de comunicação com a Validra (após retries internos)   | Compensar ou retentar o SAGA |

O adapter **nunca deixa uma mensagem sem resposta** — mesmo em caso de exceção interna, publica um `ERROR` de volta. O SAGA não precisa lidar com timeout de resposta para cenários normais, mas pode implementar um timeout de segurança como boa prática.

---

## Variáveis de Ambiente

O `transaction-service` não precisa de nenhuma variável nova. As configurações do adapter já estão no `docker-compose.yml`:

```yaml
SPRING_RABBITMQ_HOST: rabbitmq
SPRING_RABBITMQ_PORT: 5672
VALIDRA_API_KEY: ${VALIDRA_API_KEY:-STUB_KEY}
```

Para usar a Validra real, crie um `.env` na raiz do projeto:

```env
VALIDRA_API_KEY=sua_chave_aqui
```

---

## Estado atual: Stub

Enquanto a chave da Validra não estiver configurada, o adapter **sempre retorna `APPROVED`** com `similarityScore: 1.0`. Isso permite desenvolver e testar o fluxo SAGA completo sem dependência externa.

Para ativar a integração real, consulte o `README` do `biometric-adapter-service`.
