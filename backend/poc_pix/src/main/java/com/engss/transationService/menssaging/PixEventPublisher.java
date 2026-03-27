package com.engss.transationService.messaging;

import com.engss.transationService.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class PixEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PixEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PixEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSaqueIniciado(UUID accountId, UUID idempotencyKey,
                                      BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_SAQUE_INICIADO, Map.of(
            "accountId",      accountId,
            "idempotencyKey", idempotencyKey,
            "amount",         amount,
            "correlationId",  correlationId
        ));
        log.info("Publicado SaqueIniciado. correlationId={}", correlationId);
    }

    public void publishCreditoInicial(UUID accountId, UUID idempotencyKey,
                                       BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_CREDITO_INICIAL, Map.of(
            "accountId",      accountId,
            "idempotencyKey", idempotencyKey,
            "amount",         amount,
            "correlationId",  correlationId
        ));
        log.info("Publicado CreditoInicial. accountId={}", accountId);
    }

    public void publishPixConfirmado(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_PIX_CONFIRMADO, Map.of(
            "accountId",     accountId,
            "amount",        amount,
            "correlationId", correlationId
        ));
        log.info("Publicado PixConfirmado. correlationId={}", correlationId);
    }

    public void publishPixFalhou(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_PIX_FALHOU, Map.of(
            "accountId",     accountId,
            "amount",        amount,
            "correlationId", correlationId
        ));
        log.info("Publicado PixFalhou. correlationId={}", correlationId);
    }

    private void send(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                objectMapper.writeValueAsString(payload)
            );
        } catch (Exception e) {
            log.error("Erro ao publicar evento. routingKey={}", routingKey, e);
            throw new RuntimeException(e);
        }
    }
}