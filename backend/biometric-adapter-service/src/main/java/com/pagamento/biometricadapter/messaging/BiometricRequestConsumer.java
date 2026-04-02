package com.pagamento.biometricadapter.messaging;

import com.pagamento.biometricadapter.domain.BiometricRequest;
import com.pagamento.biometricadapter.service.BiometricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Consumer AMQP que escuta a fila {@code biometric.verification.request}
 * publicada pelo SAGA e delega o processamento ao {@link BiometricService}.
 *
 * <p>A fila é configurada em {@link com.pagamento.biometricadapter.config.RabbitMQConfig}.
 * O ACK é automático (padrão Spring AMQP); exceções não tratadas causam NACK
 * e roteamento para a DLQ.
 *
 * <p>O MDC é populado com o {@code transactionId} para correlacionar todos os
 * logs gerados durante o processamento da mensagem (útil com OpenTelemetry).
 */
@Component
public class BiometricRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(BiometricRequestConsumer.class);

    private static final String MDC_TRANSACTION_ID = "transactionId";
    private static final String MDC_ATTEMPT        = "attempt";

    private final BiometricService biometricService;

    public BiometricRequestConsumer(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    /**
     * Recebe e processa uma mensagem de verificação biométrica.
     *
     * @param request      payload desserializado automaticamente via {@link com.fasterxml.jackson.databind.ObjectMapper}
     * @param deliveryTag  tag de entrega do broker (header AMQP) — para logging
     */
    @RabbitListener(
            queues = "${rabbitmq.queues.request:biometric.verification.request}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(
            @Payload BiometricRequest request,
            @Header(value = "amqp_deliveryTag", required = false) Long deliveryTag) {

        setupMdc(request, deliveryTag);
        try {
            log.info("[Consumer] Mensagem recebida → transactionId={} attempt={} deliveryTag={}",
                    request.getTransactionId(), request.getAttempt(), deliveryTag);

            validate(request);
            biometricService.process(request);

            log.debug("[Consumer] Mensagem processada com sucesso → transactionId={}",
                    request.getTransactionId());

        } catch (IllegalArgumentException ex) {
            // Mensagem malformada — não deve ser reprocessada, vai para DLQ imediatamente.
            log.error("[Consumer] Mensagem inválida descartada → transactionId={} motivo={}",
                    request.getTransactionId(), ex.getMessage());
            throw ex; // NACK → DLQ

        } finally {
            MDC.clear();
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private void validate(BiometricRequest request) {
        if (request.getTransactionId() == null) {
            throw new IllegalArgumentException("transactionId é obrigatório");
        }
        if (request.getCpf() == null || request.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (request.getSelfieBase64() == null || request.getSelfieBase64().isBlank()) {
            throw new IllegalArgumentException("selfieBase64 é obrigatório");
        }
    }

    private void setupMdc(BiometricRequest request, Long deliveryTag) {
        Optional.ofNullable(request.getTransactionId())
                .ifPresent(id -> MDC.put(MDC_TRANSACTION_ID, id.toString()));
        MDC.put(MDC_ATTEMPT, String.valueOf(request.getAttempt()));
    }
}
