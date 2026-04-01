package com.engss.transaction.infraestructure.messaging;

import com.engss.transaction.domain.model.StatusTransacao;
import com.engss.transaction.domain.repository.TransacaoPixRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventConsumer.class);

    private final TransacaoPixRepository transacaoPixRepository;

    public LedgerEventConsumer(TransacaoPixRepository transacaoPixRepository) {
        this.transacaoPixRepository = transacaoPixRepository;
    }

    @RabbitListener(queues = "${app.rabbit.queue.ledger-debited}")
    @Transactional
    public void onLedgerDebited(Map<String, Object> payload) {
        try {
            Object correlationIdObj = payload.get("correlationId");
            if (correlationIdObj == null) {
                log.warn("Evento ledger.debited sem correlationId. payload={}", payload);
                return;
            }

            String correlationId = correlationIdObj.toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresentOrElse(transacao -> {
                transacao.setStatus(StatusTransacao.EM_PROCESSAMENTO);
                transacaoPixRepository.save(transacao);

                log.info("Transação atualizada para EM_PROCESSAMENTO. correlationId={}", correlationId);
            }, () -> log.warn("Transação não encontrada para ledger.debited. correlationId={}", correlationId));

        } catch (Exception e) {
            log.error("Erro ao processar evento ledger.debited. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${app.rabbit.queue.ledger-debit-confirmed}")
    @Transactional
    public void onLedgerDebitConfirmed(Map<String, Object> payload) {
        try {
            Object correlationIdObj = payload.get("correlationId");
            if (correlationIdObj == null) {
                log.warn("Evento ledger.debit.confirmed sem correlationId. payload={}", payload);
                return;
            }

            String correlationId = correlationIdObj.toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresentOrElse(transacao -> {
                transacao.setStatus(StatusTransacao.CONCLUIDA);
                transacaoPixRepository.save(transacao);

                log.info("Transação atualizada para CONCLUIDA. correlationId={}", correlationId);
            }, () -> log.warn("Transação não encontrada para ledger.debit.confirmed. correlationId={}", correlationId));

        } catch (Exception e) {
            log.error("Erro ao processar evento ledger.debit.confirmed. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${app.rabbit.queue.ledger-reversed}")
    @Transactional
    public void onLedgerReversed(Map<String, Object> payload) {
        try {
            Object correlationIdObj = payload.get("correlationId");
            if (correlationIdObj == null) {
                log.warn("Evento ledger.reversed sem correlationId. payload={}", payload);
                return;
            }

            String correlationId = correlationIdObj.toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresentOrElse(transacao -> {
                transacao.setStatus(StatusTransacao.FALHA);
                transacaoPixRepository.save(transacao);

                log.info("Transação atualizada para FALHA. correlationId={}", correlationId);
            }, () -> log.warn("Transação não encontrada para ledger.reversed. correlationId={}", correlationId));

        } catch (Exception e) {
            log.error("Erro ao processar evento ledger.reversed. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }
}