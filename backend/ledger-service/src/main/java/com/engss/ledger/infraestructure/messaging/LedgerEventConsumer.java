package com.engss.ledger.infraestructure.messaging;

import com.engss.ledger.application.command.DebitCommand;
import com.engss.ledger.application.command.ReversalCommand;
import com.engss.ledger.application.service.LedgerCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventConsumer.class);

    private final LedgerCommandService ledgerCommandService;

    @RabbitListener(queues = "${ledger.queue.saque-iniciado}")
    public void onSaqueIniciado(Map<String, Object> payload) {
        try {
            var cmd = new DebitCommand(
                UUID.fromString(payload.get("accountId").toString()),
                UUID.fromString(payload.get("idempotencyKey").toString()),
                new BigDecimal(payload.get("amount").toString()),
                UUID.fromString(payload.get("correlationId").toString())
            );
            ledgerCommandService.debitPending(cmd);
            log.info("DEBIT_PENDING aplicado. correlationId={}", cmd.correlationId());
        } catch (Exception e) {
            log.error("Erro ao processar SaqueIniciado. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${ledger.queue.pix-confirmado}")
    public void onPixConfirmado(Map<String, Object> payload) {
        try {
            var correlationId = UUID.fromString(payload.get("correlationId").toString());
            var accountId     = UUID.fromString(payload.get("accountId").toString());
            var amount        = new BigDecimal(payload.get("amount").toString());
            ledgerCommandService.confirmDebit(correlationId, accountId, amount);
            log.info("DEBIT_CONFIRMED aplicado. correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("Erro ao processar PixConfirmado. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${ledger.queue.pix-falhou}")
    public void onPixFalhou(Map<String, Object> payload) {
        try {
            var cmd = new ReversalCommand(
                UUID.fromString(payload.get("accountId").toString()),
                UUID.fromString(payload.get("correlationId").toString())
            );
            ledgerCommandService.reverse(cmd);
            log.info("REVERSAL aplicado. correlationId={}", cmd.correlationId());
        } catch (Exception e) {
            log.error("Erro ao processar PixFalhou. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${ledger.queue.credito-inicial}")
    public void onCreditoInicial(Map<String, Object> payload) {
        try {
            var accountId      = UUID.fromString(payload.get("accountId").toString());
            var idempotencyKey = UUID.fromString(payload.get("idempotencyKey").toString());
            var amount         = new BigDecimal(payload.get("amount").toString());
            var correlationId  = UUID.fromString(payload.get("correlationId").toString());

            var cmd = new DebitCommand(accountId, idempotencyKey, amount, correlationId);
            ledgerCommandService.creditInitial(cmd);
            log.info("CREDIT_INITIAL aplicado. accountId={}", accountId);
        } catch (Exception e) {
            log.error("Erro ao processar CreditoInicial. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }
}