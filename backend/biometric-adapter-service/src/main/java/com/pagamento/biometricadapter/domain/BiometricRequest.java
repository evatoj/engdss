package com.pagamento.biometricadapter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Mensagem de solicitação de verificação biométrica publicada pelo SAGA
 * na fila biometric.verification.request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BiometricRequest {

    /** Identificador único da transação no SAGA — correlação de toda a cadeia. */
    private UUID transactionId;

    /** CPF do portador a ser verificado. Formato: apenas dígitos (11 chars). */
    private String cpf;

    /**
     * Foto selfie do portador codificada em Base64 (JPEG ou PNG).
     * Será enviada à Validra para comparação facial com a base Serpro/Datavalid.
     */
    private String selfieBase64;

    /** Momento em que o SAGA publicou a solicitação (UTC). Útil para SLA / timeout. */
    private Instant requestedAt;

    /**
     * Tentativa atual — o SAGA pode reprocessar em caso de ERROR.
     * Começa em 1; política de retry definida no SAGA.
     */
    @Builder.Default
    private int attempt = 1;
}
