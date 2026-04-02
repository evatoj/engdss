package com.pagamento.biometricadapter.adapter.fee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta recebida da Validra após verificação facial.
 *
 * <p><b>TODO:</b> Confirmar todos os campos na documentação oficial:
 * <a href="https://web-production-03687.up.railway.app/docs">Validra Docs</a>
 *
 * <p>Campos marcados com {@code // TODO} indicam que o nome/tipo pode variar
 * conforme a versão da API. O mapeamento de status para {@code BiometricStatus}
 * ocorre em {@link com.pagamento.biometricadapter.adapter.fee.FeeClient}.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeVerifyResponse {

    /**
     * Status textual retornado pela Validra.
     * Valores esperados (TODO — confirmar): "approved" | "rejected" | "error"
     */
    @JsonProperty("status")
    private String status;

    /**
     * Score de similaridade facial (0.0 – 1.0).
     * Quanto mais próximo de 1.0, maior a confiança na correspondência.
     * TODO: Confirmar nome do campo e escala (pode ser 0–100).
     */
    @JsonProperty("similarity_score")
    private Double similarityScore;

    /**
     * Motivo legível em caso de rejeição ou erro.
     * Ex: "face_mismatch", "low_quality_image", "cpf_not_found".
     * TODO: Confirmar nome do campo.
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * ID de rastreabilidade interno da Validra.
     * Útil para suporte e auditoria. TODO: Confirmar existência/nome.
     */
    @JsonProperty("request_id")
    private String requestId;
}
