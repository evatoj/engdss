package com.engss.ledger.application.service;

import com.engss.ledger.application.command.DebitCommand;
import com.engss.ledger.application.command.ReversalCommand;
import com.engss.ledger.application.exception.InsufficientBalanceException;
import com.engss.ledger.domain.model.*;
import com.engss.ledger.domain.repository.*;
import com.engss.ledger.infraestructure.outbox.OutboxEvent;
import com.engss.ledger.infraestructure.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerCommandService {

    private final LedgerEventRepository ledgerEventRepository;
    private final BalanceViewRepository balanceViewRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void debitPending(DebitCommand cmd) {
        var balance = getOrCreateBalance(cmd.accountId());
        if (balance.getAvailableBalance().compareTo(cmd.amount()) < 0) {
            throw new InsufficientBalanceException(cmd.accountId(), cmd.amount());
        }
        var event = LedgerEvent.debitPending(
            cmd.accountId(), cmd.idempotencyKey(), cmd.amount(), cmd.correlationId()
        );
        ledgerEventRepository.save(event);
        balance.apply(event);
        balanceViewRepository.save(balance);
        saveOutbox("LedgerDebited", cmd.correlationId(), "ledger.debited");
    }

    @Transactional
    public void confirmDebit(UUID correlationId, UUID accountId, BigDecimal amount) {
        var pendingEvent = ledgerEventRepository
            .findByCorrelationIdAndType(correlationId, LedgerEventType.DEBIT_PENDING)
            .orElseThrow(() -> new IllegalStateException("DEBIT_PENDING não encontrado: " + correlationId));
        var confirmedEvent = LedgerEvent.debitConfirmed(
            accountId, pendingEvent.getIdempotencyKey(), amount, correlationId
        );
        var balance = getOrCreateBalance(accountId);
        ledgerEventRepository.save(confirmedEvent);
        balance.apply(confirmedEvent);
        balanceViewRepository.save(balance);
        saveOutbox("LedgerDebitConfirmed", correlationId, "ledger.debit.confirmed");
    }

    @Transactional
    public void reverse(ReversalCommand cmd) {
        var pendingEvent = ledgerEventRepository
            .findByCorrelationIdAndType(cmd.correlationId(), LedgerEventType.DEBIT_PENDING)
            .orElseThrow(() -> new IllegalStateException("Nada a estornar: " + cmd.correlationId()));
        var reversalEvent = LedgerEvent.reversal(
            cmd.accountId(), pendingEvent.getIdempotencyKey(),
            pendingEvent.getAmount(), cmd.correlationId()
        );
        var balance = getOrCreateBalance(cmd.accountId());
        ledgerEventRepository.save(reversalEvent);
        balance.apply(reversalEvent);
        balanceViewRepository.save(balance);
        saveOutbox("LedgerReversed", cmd.correlationId(), "ledger.reversed");
    }

    @Transactional
    public void creditInitial(DebitCommand cmd) {
        var balance = getOrCreateBalance(cmd.accountId());
        var event = LedgerEvent.credit(
            cmd.accountId(), cmd.idempotencyKey(), cmd.amount(), cmd.correlationId()
        );
        ledgerEventRepository.save(event);
        balance.apply(event);
        balanceViewRepository.save(balance);
    }

    private BalanceView getOrCreateBalance(UUID accountId) {
        return balanceViewRepository.findByIdForUpdate(accountId)
            .orElseGet(() -> new BalanceView(accountId, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now(), null));
    }

    // payload simples sem ObjectMapper — evita double serialization
    private void saveOutbox(String eventType, UUID correlationId, String routingKey) {
        String payload = "{\"correlationId\":\"" + correlationId + "\"}";
        outboxEventRepository.save(OutboxEvent.of(eventType, payload, routingKey));
    }
}