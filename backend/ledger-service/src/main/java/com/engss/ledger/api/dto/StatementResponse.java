package com.engss.ledger.api.dto;
import com.engss.ledger.domain.model.LedgerEventType;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;

public record StatementResponse(
  UUID eventId, 
  LedgerEventType type, 
  BigDecimal amount, 
  Instant occurredAt, 
  UUID correlationId
) {}