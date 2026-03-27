package com.engss.ledgerService.api.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record DebitRequest(
    @NotNull UUID accountId,
    @NotNull @Positive BigDecimal amount
) {}