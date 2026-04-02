package com.pagamento.biometricadapter.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado da verificação biométrica publicado pelo adapter
 * na fila biometric.verification.response para o SAGA continuar o fluxo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricResponse {

    /** Mesmo ID da {@link BiometricRequest} — correlação SAGA. */
    private UUID transactionId;

    /** Resultado da verificação: APPROVED | REJECTED | ERROR. */
    private BiometricStatus status;

    /**
     * Descrição legível do resultado.
     * Em REJECTED ou ERROR, contém o motivo (ex: "face_mismatch", "timeout").
     */
    private String message;

    /**
     * Score de similaridade facial retornado pela Validra (0.0 – 1.0).
     * {@code null} quando o status for ERROR (chamada não completou).
     */
    private Double similarityScore;

    /** Momento em que a resposta foi gerada pelo adapter (UTC). */
    @Builder.Default
    private Instant respondedAt = Instant.now();

    // ------------------------------------------------------------------ //
    //  Factory helpers                                                      //
    // ------------------------------------------------------------------ //

    public static BiometricResponse approved(UUID transactionId, Double score) {
        return BiometricResponse.builder()
                .transactionId(transactionId)
                .status(BiometricStatus.APPROVED)
                .message("Biometric verification approved")
                .similarityScore(score)
                .build();
    }

    public static BiometricResponse rejected(UUID transactionId, Double score, String reason) {
        return BiometricResponse.builder()
                .transactionId(transactionId)
                .status(BiometricStatus.REJECTED)
                .message(reason)
                .similarityScore(score)
                .build();
    }

    public static BiometricResponse error(UUID transactionId, String reason) {
        return BiometricResponse.builder()
                .transactionId(transactionId)
                .status(BiometricStatus.ERROR)
                .message(reason)
                .build();
    }
}
