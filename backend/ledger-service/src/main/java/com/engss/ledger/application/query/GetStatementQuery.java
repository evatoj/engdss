package com.engss.ledger.application.query;

import java.time.Instant;
import java.util.UUID;

public record GetStatementQuery(
    UUID accountId,
    Instant from,   // nullable — filtra por período se informado
    Instant to      // nullable
) {
    // construtor conveniente sem filtro de data
    public static GetStatementQuery of(UUID accountId) {
        return new GetStatementQuery(accountId, null, null);
    }
}