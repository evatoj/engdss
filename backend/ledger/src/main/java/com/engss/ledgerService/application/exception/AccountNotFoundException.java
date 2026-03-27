package com.engss.ledgerService.application.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID accountId) {
        super("Conta não encontrada: " + accountId);
    }
}