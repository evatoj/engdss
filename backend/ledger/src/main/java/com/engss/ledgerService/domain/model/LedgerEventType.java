package com.engss.ledgerService.domain.model;

public enum LedgerEventType {
    DEBIT_PENDING,    // saldo bloqueado, aguardando confirmação PIX
    DEBIT_CONFIRMED,  // PIX confirmado, débito efetivado
    CREDIT,           // crédito na conta
    REVERSAL          // estorno — compensa um DEBIT_PENDING quando PIX falha
}