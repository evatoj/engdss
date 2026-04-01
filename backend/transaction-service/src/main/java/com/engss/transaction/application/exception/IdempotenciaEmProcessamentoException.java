package com.engss.transaction.application.exception;

public class IdempotenciaEmProcessamentoException extends RuntimeException {

    public IdempotenciaEmProcessamentoException(String message) {
        super(message);
    }
}