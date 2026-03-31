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

    @RabbitListener(queues = "transaction.ledger.debited")
    @Transactional
    public void onLedgerDebited(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.EM_PROCESSAMENTO);
                transacaoPixRepository.save(transacao);

                log.info("Transacao EM_PROCESSAMENTO. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebited. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "transaction.ledger.debit.confirmed")
    @Transactional
    public void onLedgerDebitConfirmed(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.CONCLUIDA);
                transacaoPixRepository.save(transacao);

                log.info("Transacao CONCLUIDA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebitConfirmed. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "transaction.ledger.reversed")
    @Transactional
    public void onLedgerReversed(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.FALHA);
                transacaoPixRepository.save(transacao);

                log.info("Transacao FALHA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerReversed. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }
}