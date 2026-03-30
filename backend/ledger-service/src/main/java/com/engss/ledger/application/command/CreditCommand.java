package com.engss.ledger.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCommand(
    UUID accountId,
    UUID idempotencyKey,
    BigDecimal amount,
    UUID correlationId
) {}