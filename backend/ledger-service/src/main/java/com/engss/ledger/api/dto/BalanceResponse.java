package com.engss.ledger.api.dto;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
public record BalanceResponse(
  UUID accountId, 
  BigDecimal availableBalance, 
  BigDecimal pendingBalance, 
  Instant updatedAt
) {}
