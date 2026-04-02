package com.pagamento.biometricadapter.service;

import com.pagamento.biometricadapter.adapter.fee.FeeClient;
import com.pagamento.biometricadapter.adapter.fee.FeeClientException;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyRequest;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyResponse;
import com.pagamento.biometricadapter.domain.BiometricRequest;
import com.pagamento.biometricadapter.domain.BiometricResponse;
import com.pagamento.biometricadapter.domain.BiometricStatus;
import com.pagamento.biometricadapter.messaging.BiometricResponsePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orquestra o fluxo de verificação biométrica:
 * <ol>
 *   <li>Converte {@link BiometricRequest} (domínio interno) → {@link FeeVerifyRequest} (DTO externo).</li>
 *   <li>Chama a Validra via {@link FeeClient}.</li>
 *   <li>Converte {@link FeeVerifyResponse} → {@link BiometricResponse}.</li>
 *   <li>Publica o resultado via {@link BiometricResponsePublisher}.</li>
 * </ol>
 *
 * <p>Erros de comunicação com a Validra são capturados e convertidos em
 * resposta com status {@link BiometricStatus#ERROR}, evitando NACK e DLQ
 * para falhas transitórias que já passaram pela política de retry do {@link FeeClient}.
 */
@Service
public class BiometricService {

    private static final Logger log = LoggerFactory.getLogger(BiometricService.class);

    private final FeeClient feeClient;
    private final BiometricResponsePublisher publisher;

    public BiometricService(FeeClient feeClient, BiometricResponsePublisher publisher) {
        this.feeClient = feeClient;
        this.publisher = publisher;
    }

    /**
     * Processa uma solicitação de verificação biométrica recebida do SAGA.
     *
     * @param request mensagem desserializada da fila {@code biometric.verification.request}
     */
    public void process(BiometricRequest request) {
        log.info("[Service] Processando verificação biométrica → transactionId={} attempt={}",
                request.getTransactionId(), request.getAttempt());

        BiometricResponse response;
        try {
            FeeVerifyRequest feeRequest = toFeeRequest(request);

            // Bloco — chamada reativa executada de forma síncrona no contexto do consumer AMQP.
            // O FeeClient já aplica retry internamente via retrySpec (backoff + jitter).
            FeeVerifyResponse feeResponse = feeClient.verify(feeRequest).block();

            response = toResponse(request, feeResponse);

        } catch (FeeClientException ex) {
            log.error("[Service] Falha na comunicação com Validra → transactionId={} http={} msg={}",
                    request.getTransactionId(),
                    ex.getHttpStatus() != null ? ex.getHttpStatus().value() : "N/A",
                    ex.getMessage());
            response = BiometricResponse.error(request.getTransactionId(), buildErrorMessage(ex));

        } catch (Exception ex) {
            log.error("[Service] Erro inesperado → transactionId={}", request.getTransactionId(), ex);
            response = BiometricResponse.error(request.getTransactionId(), "internal_error");
        }

        publisher.publish(response);
    }

    // ------------------------------------------------------------------ //
    //  Mapeamentos                                                         //
    // ------------------------------------------------------------------ //

    private FeeVerifyRequest toFeeRequest(BiometricRequest request) {
        return FeeVerifyRequest.builder()
                .cpf(request.getCpf())
                .selfieBase64(request.getSelfieBase64())
                .build();
    }

    private BiometricResponse toResponse(BiometricRequest request, FeeVerifyResponse feeResponse) {
        if (feeResponse == null) {
            log.warn("[Service] FeeVerifyResponse nulo → transactionId={}", request.getTransactionId());
            return BiometricResponse.error(request.getTransactionId(), "empty_response_from_provider");
        }

        BiometricStatus status = feeClient.mapStatus(feeResponse);
        Double score = feeResponse.getSimilarityScore();
        String reason = feeResponse.getReason();

        log.info("[Service] Resultado Validra → transactionId={} status={} score={}",
                request.getTransactionId(), status, score);

        return switch (status) {
            case APPROVED -> BiometricResponse.approved(request.getTransactionId(), score);
            case REJECTED -> BiometricResponse.rejected(
                    request.getTransactionId(), score,
                    reason != null ? reason : "face_mismatch");
            case ERROR    -> BiometricResponse.error(
                    request.getTransactionId(),
                    reason != null ? reason : "provider_error");
        };
    }

    private String buildErrorMessage(FeeClientException ex) {
        if (ex.isClientError()) {
            return "provider_client_error_%d".formatted(ex.getHttpStatus().value());
        }
        if (ex.isServerError()) {
            return "provider_server_error_%d".formatted(ex.getHttpStatus().value());
        }
        return "provider_communication_failure";
    }
}
