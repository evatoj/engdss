package com.engss.ledger.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_events")
public class LedgerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEventType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    private UUID correlationId;

    protected LedgerEvent() {}

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public LedgerEventType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Instant getOccurredAt() { return occurredAt; }
    public UUID getCorrelationId() { return correlationId; }

    public static LedgerEvent debitPending(UUID accountId, UUID idempotencyKey,
                                           BigDecimal amount, UUID correlationId) {
        return build(accountId, idempotencyKey, LedgerEventType.DEBIT_PENDING, amount, correlationId);
    }

    public static LedgerEvent debitConfirmed(UUID accountId, UUID idempotencyKey,
                                             BigDecimal amount, UUID correlationId) {
        return build(accountId, idempotencyKey, LedgerEventType.DEBIT_CONFIRMED, amount, correlationId);
    }

    public static LedgerEvent credit(UUID accountId, UUID idempotencyKey,
                                     BigDecimal amount, UUID correlationId) {
        return build(accountId, idempotencyKey, LedgerEventType.CREDIT, amount, correlationId);
    }
    public static LedgerEvent debitDenied(UUID accountId, UUID idempotencyKey,
                                          BigDecimal amount, UUID correlationId) {
        return build(accountId, idempotencyKey, LedgerEventType.DEBIT_DENIED, amount, correlationId);
    }

    public static LedgerEvent reversal(UUID accountId, UUID idempotencyKey,
                                       BigDecimal amount, UUID correlationId) {
        return build(accountId, idempotencyKey, LedgerEventType.REVERSAL, amount, correlationId);
    }

    private static LedgerEvent build(UUID accountId, UUID idempotencyKey,
                                     LedgerEventType type, BigDecimal amount, UUID correlationId) {
        var event = new LedgerEvent();
        event.accountId = accountId;
        event.idempotencyKey = idempotencyKey;
        event.type = type;
        event.amount = amount;
        event.correlationId = correlationId;
        event.occurredAt = Instant.now();
        return event;
    }
}