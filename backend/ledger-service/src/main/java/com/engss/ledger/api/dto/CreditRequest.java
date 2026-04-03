package com.engss.ledger.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditRequest(
    @NotNull UUID accountId,
    @NotNull @Positive BigDecimal amount
) {}