package com.pagamento.biometricadapter.messaging;

import com.pagamento.biometricadapter.domain.BiometricResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publica o resultado da verificação biométrica de volta ao SAGA
 * via RabbitMQ na fila {@code biometric.verification.response}.
 */
@Component
public class BiometricResponsePublisher {

    private static final Logger log = LoggerFactory.getLogger(BiometricResponsePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String responseQueue;

    public BiometricResponsePublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.queues.response:biometric.verification.response}") String responseQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.responseQueue = responseQueue;
    }

    /**
     * Publica a resposta no exchange padrão (configurado no {@link RabbitTemplate})
     * com a routing key da fila de resposta.
     *
     * @param response resultado da verificação (APPROVED / REJECTED / ERROR)
     */
    public void publish(BiometricResponse response) {
        log.info("[Publisher] Publicando resposta → transactionId={} status={}",
                response.getTransactionId(), response.getStatus());

        try {
            rabbitTemplate.convertAndSend(responseQueue, response);
            log.debug("[Publisher] Mensagem publicada com sucesso → {}", response.getTransactionId());
        } catch (Exception ex) {
            // Lançar aqui causará NACK na mensagem original — avalie a política de DLQ.
            log.error("[Publisher] Falha ao publicar resposta para transactionId={}: {}",
                    response.getTransactionId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
