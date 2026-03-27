package com.engss.ledgerService.infraestructure.messaging;

import com.engss.ledgerService.infraestructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishLedgerDebited(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_DEBITED, Map.of(
            "accountId",     accountId,
            "amount",        amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerDebited. correlationId={}", correlationId);
    }

    public void publishLedgerDebitConfirmed(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_DEBIT_CONFIRMED, Map.of(
            "accountId",     accountId,
            "amount",        amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerDebitConfirmed. correlationId={}", correlationId);
    }

    public void publishLedgerReversed(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_REVERSED, Map.of(
            "accountId",     accountId,
            "amount",        amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerReversed. correlationId={}", correlationId);
    }

    @SneakyThrows
    private void send(String routingKey, Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            routingKey,
            objectMapper.writeValueAsString(payload)
        );
    }
}