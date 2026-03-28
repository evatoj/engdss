package com.engss.transationService.messaging;

import com.engss.transationService.config.RabbitMQConfig;
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

    public PixEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSaqueIniciado(UUID accountId, UUID idempotencyKey,
                                      BigDecimal amount, UUID correlationId) {
        Map<String, Object> payload = Map.of(
            "accountId",      accountId.toString(),
            "idempotencyKey", idempotencyKey.toString(),
            "amount",         amount,
            "correlationId",  correlationId.toString()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_SAQUE_INICIADO, payload);
        log.info("Publicado SaqueIniciado. correlationId={}", correlationId);
    }

    public void publishCreditoInicial(UUID accountId, UUID idempotencyKey,
                                       BigDecimal amount, UUID correlationId) {
        Map<String, Object> payload = Map.of(
            "accountId",      accountId.toString(),
            "idempotencyKey", idempotencyKey.toString(),
            "amount",         amount,
            "correlationId",  correlationId.toString()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CREDITO_INICIAL, payload);
        log.info("Publicado CreditoInicial. accountId={}", accountId);
    }

    public void publishPixConfirmado(UUID accountId, BigDecimal amount, UUID correlationId) {
        Map<String, Object> payload = Map.of(
            "accountId",     accountId.toString(),
            "amount",        amount,
            "correlationId", correlationId.toString()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PIX_CONFIRMADO, payload);
        log.info("Publicado PixConfirmado. correlationId={}", correlationId);
    }

    public void publishPixFalhou(UUID accountId, BigDecimal amount, UUID correlationId) {
        Map<String, Object> payload = Map.of(
            "accountId",     accountId.toString(),
            "amount",        amount,
            "correlationId", correlationId.toString()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PIX_FALHOU, payload);
        log.info("Publicado PixFalhou. correlationId={}", correlationId);
    }
}