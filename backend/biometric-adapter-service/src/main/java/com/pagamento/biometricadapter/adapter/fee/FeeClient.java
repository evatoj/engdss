package com.pagamento.biometricadapter.adapter.fee;

import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyRequest;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyResponse;
import com.pagamento.biometricadapter.domain.BiometricStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Cliente HTTP para a <b>Validra API</b> — verificação facial biométrica.
 *
 * <h2>Estado atual: STUB</h2>
 * <p>O método {@link #verify(FeeVerifyRequest)} <b>não realiza chamada HTTP</b>.
 * Sempre retorna {@code APPROVED} com score {@code 1.0} para viabilizar o
 * desenvolvimento e testes de integração do fluxo SAGA sem dependência externa.
 *
 * <h2>Como ativar a integração real</h2>
 * <ol>
 *   <li>Ajuste os nomes de campo em {@link FeeVerifyRequest} e
 *       {@link FeeVerifyResponse} conforme a doc da Validra.</li>
 *   <li>Confirme o path do endpoint em {@code validra.endpoint.facial}.</li>
 *   <li>Remova o bloco marcado com {@code // ── STUB ──} abaixo e
 *       descomente o bloco {@code // ── REAL ──}.</li>
 *   <li>Configure {@code VALIDRA_API_KEY} como variável de ambiente.</li>
 * </ol>
 *
 * @see FeeClientConfig
 * @see FeeVerifyRequest
 * @see FeeVerifyResponse
 */
@Component
public class FeeClient {

    private static final Logger log = LoggerFactory.getLogger(FeeClient.class);

    /** Score mínimo para considerar a verificação como APPROVED. */
    private static final double APPROVAL_THRESHOLD = 0.75;

    private final WebClient webClient;
    private final Retry retrySpec;
    private final String facialEndpoint;

    public FeeClient(
            @Qualifier("validraWebClient") WebClient webClient,
            @Qualifier("validraRetrySpec") Retry retrySpec,
            @Value("${validra.endpoint.facial:/v1/biometrics/facial}") String facialEndpoint) {
        this.webClient = webClient;
        this.retrySpec = retrySpec;
        this.facialEndpoint = facialEndpoint;
    }

    // ================================================================== //
    //  Public API                                                          //
    // ================================================================== //

    /**
     * Submete a selfie do portador para verificação facial contra a base Serpro/Datavalid.
     *
     * @param request CPF + selfie Base64
     * @return {@link FeeVerifyResponse} com status, score e motivo (quando rejeitado/erro)
     * @throws FeeClientException em caso de erro HTTP, timeout ou falha de desserialização
     */
    public Mono<FeeVerifyResponse> verify(FeeVerifyRequest request) {

        // ── STUB ────────────────────────────────────────────────────────
        // Remove este bloco ao integrar com a Validra real.
        log.warn("[FeeClient][STUB] Retornando APPROVED sem chamar Validra. " +
                 "transactionId não disponível neste nível — rastrear via CPF: {}", request.getCpf());

        FeeVerifyResponse stubResponse = new FeeVerifyResponse();
        stubResponse.setStatus("approved");
        stubResponse.setSimilarityScore(1.0);
        stubResponse.setReason(null);
        stubResponse.setRequestId("STUB-" + System.nanoTime());
        return Mono.just(stubResponse);
        // ── FIM STUB ─────────────────────────────────────────────────────

        /*
        // ── REAL ─────────────────────────────────────────────────────────
        // Descomente este bloco após ajustar os DTOs conforme a doc da Validra.

        log.debug("[Validra] Iniciando verificação facial para CPF: {}", maskCpf(request.getCpf()));

        return webClient.post()
                .uri(facialEndpoint)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new FeeClientException(
                                        "Validra rejected the request [%d]: %s"
                                                .formatted(clientResponse.statusCode().value(), body),
                                        clientResponse.statusCode()
                                )))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new FeeClientException(
                                        "Validra server error [%d]: %s"
                                                .formatted(clientResponse.statusCode().value(), body),
                                        clientResponse.statusCode()
                                )))
                )
                .bodyToMono(FeeVerifyResponse.class)
                .retryWhen(retrySpec)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new FeeClientException("Unexpected HTTP error from Validra", ex.getStatusCode(), ex))
                .onErrorMap(ex -> !(ex instanceof FeeClientException),
                        ex -> new FeeClientException("Communication failure with Validra", ex));
        // ── FIM REAL ──────────────────────────────────────────────────────
        */
    }

    // ================================================================== //
    //  Status mapping                                                      //
    // ================================================================== //

    /**
     * Mapeia o status textual da Validra para o {@link BiometricStatus} interno.
     *
     * <p>TODO: Ajustar os valores de string conforme a resposta real da Validra.
     *
     * @param response resposta desserializada da Validra
     * @return status interno
     */
    public BiometricStatus mapStatus(FeeVerifyResponse response) {
        if (response == null || response.getStatus() == null) {
            return BiometricStatus.ERROR;
        }

        return switch (response.getStatus().toLowerCase()) {
            case "approved", "match", "verified" -> {
                // Dupla verificação por score quando disponível
                Double score = response.getSimilarityScore();
                yield (score == null || score >= APPROVAL_THRESHOLD)
                        ? BiometricStatus.APPROVED
                        : BiometricStatus.REJECTED;
            }
            case "rejected", "no_match", "mismatch" -> BiometricStatus.REJECTED;
            default -> BiometricStatus.ERROR;
        };
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /** Mascara CPF para logs: {@code 123.***.***-45}. */
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}
