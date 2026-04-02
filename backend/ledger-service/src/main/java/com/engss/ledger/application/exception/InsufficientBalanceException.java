package com.engss.ledger.application.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(UUID accountId, BigDecimal amount) {
        super("Saldo insuficiente para conta " + accountId + ". Solicitado: " + amount, null, false, false);
    }
}