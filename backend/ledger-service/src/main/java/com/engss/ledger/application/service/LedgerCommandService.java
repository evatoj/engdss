package com.engss.ledger.application.service;

import com.engss.ledger.application.command.CreditCommand;
import com.engss.ledger.application.command.DebitCommand;
import com.engss.ledger.application.command.ReversalCommand;
import com.engss.ledger.domain.model.BalanceView;
import com.engss.ledger.domain.model.LedgerEvent;
import com.engss.ledger.domain.model.LedgerEventType;
import com.engss.ledger.domain.repository.BalanceViewRepository;
import com.engss.ledger.domain.repository.LedgerEventRepository;
import com.engss.ledger.infraestructure.outbox.OutboxEvent;
import com.engss.ledger.infraestructure.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerCommandService {

    private static final Logger log = LoggerFactory.getLogger(LedgerCommandService.class);

    private final LedgerEventRepository ledgerEventRepository;
    private final BalanceViewRepository balanceViewRepository;
    private final OutboxEventRepository outboxEventRepository;

    /**
     * Tenta bloquear o saldo para débito.
     *
     * @return true  — saldo suficiente, DEBIT_PENDING registrado, evento enfileirado para o Rabbit.
     *         false — saldo insuficiente, DEBIT_DENIED registrado, nenhuma mensagem enviada ao Rabbit.
     */
    @Transactional
    public boolean debitPending(DebitCommand cmd) {
        if (ledgerEventRepository.existsByIdempotencyKey(cmd.idempotencyKey())) {
            // idempotência: descobre se a tentativa anterior foi aprovada ou negada
            return ledgerEventRepository
                    .findByIdempotencyKeyAndType(cmd.idempotencyKey(), LedgerEventType.DEBIT_PENDING)
                    .isPresent();
        }

        var balance = getOrCreateBalance(cmd.accountId());

        if (balance.getAvailableBalance().compareTo(cmd.amount()) < 0) {
            // --- SALDO INSUFICIENTE: registra evento e NÃO publica no Rabbit ---
            var deniedEvent = LedgerEvent.debitDenied(
                    cmd.accountId(), cmd.idempotencyKey(), cmd.amount(), cmd.correlationId()
            );
            ledgerEventRepository.save(deniedEvent);
            // BalanceView.apply(DEBIT_DENIED) não altera saldo — apenas atualiza updatedAt
            balance.apply(deniedEvent);
            balanceViewRepository.save(balance);

            saveOutbox("LedgerDebitDenied", cmd.correlationId(), "ledger.debit.denied");
            return false;
        }

        // --- SALDO SUFICIENTE: bloqueia e enfileira para o Rabbit ---
        var event = LedgerEvent.debitPending(
                cmd.accountId(), cmd.idempotencyKey(), cmd.amount(), cmd.correlationId()
        );

        ledgerEventRepository.save(event);
        balance.apply(event);
        balanceViewRepository.save(balance);

        saveOutbox("LedgerDebited", cmd.correlationId(), "ledger.debited");
        return true;
    }

    @Transactional
    public void confirmDebit(UUID correlationId, UUID accountId, BigDecimal amount) {
        // Idempotência: já confirmado anteriormente
        if (ledgerEventRepository
                .findByCorrelationIdAndType(correlationId, LedgerEventType.DEBIT_CONFIRMED)
                .isPresent()) {
            return;
        }

        // Guard: transação foi negada — não existe DEBIT_PENDING para confirmar
        if (ledgerEventRepository
                .findByCorrelationIdAndType(correlationId, LedgerEventType.DEBIT_DENIED)
                .isPresent()) {
            log.warn("confirmDebit ignorado — transação foi negada. correlationId={}", correlationId);
            return;
        }

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
        // Idempotência: já revertido anteriormente
        if (ledgerEventRepository
                .findByCorrelationIdAndType(cmd.correlationId(), LedgerEventType.REVERSAL)
                .isPresent()) {
            return;
        }

        // Guard: transação foi negada — não há saldo bloqueado para estornar
        if (ledgerEventRepository
                .findByCorrelationIdAndType(cmd.correlationId(), LedgerEventType.DEBIT_DENIED)
                .isPresent()) {
            log.warn("reverse ignorado — transação foi negada, nada a estornar. correlationId={}", cmd.correlationId());
            return;
        }

        var pendingEvent = ledgerEventRepository
                .findByCorrelationIdAndType(cmd.correlationId(), LedgerEventType.DEBIT_PENDING)
                .orElseThrow(() -> new IllegalStateException("Nada a estornar: " + cmd.correlationId()));

        var reversalEvent = LedgerEvent.reversal(
                cmd.accountId(),
                pendingEvent.getIdempotencyKey(),
                pendingEvent.getAmount(),
                cmd.correlationId()
        );

        var balance = getOrCreateBalance(cmd.accountId());
        ledgerEventRepository.save(reversalEvent);
        balance.apply(reversalEvent);
        balanceViewRepository.save(balance);

        saveOutbox("LedgerReversed", cmd.correlationId(), "ledger.reversed");
    }

    @Transactional
    public void creditInitial(CreditCommand cmd) {
        if (ledgerEventRepository.existsByIdempotencyKey(cmd.idempotencyKey())) {
            return;
        }

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

    private void saveOutbox(String eventType, UUID correlationId, String routingKey) {
        String payload = "{\"correlationId\":\"" + correlationId + "\"}";
        outboxEventRepository.save(OutboxEvent.of(eventType, payload, routingKey));
    }
}