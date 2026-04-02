package com.pagamento.biometricadapter.adapter.fee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload enviado à Validra no endpoint de verificação facial.
 *
 * <p>Endpoint esperado (a confirmar na doc oficial):
 * {@code POST https://api.validra.com.br/v1/biometrics/facial}
 *
 * <p>Campos mapeados conforme:
 * <a href="https://web-production-03687.up.railway.app/docs">Validra Docs</a>
 *
 * <p><b>TODO:</b> Ajustar nomes de campo conforme documentação real da Validra.
 * O stub ignora este objeto e sempre retorna APPROVED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeVerifyRequest {

    /**
     * CPF do portador (apenas dígitos, 11 chars).
     * A Validra usa esse dado para consultar a base Serpro/Datavalid.
     */
    @JsonProperty("cpf")
    private String cpf;

    /**
     * Foto selfie do portador codificada em Base64.
     * Formato aceito: JPEG ou PNG, resolução mínima recomendada: 480x640.
     *
     * <p><b>TODO:</b> Validar se a Validra espera prefix data URI
     * (ex: {@code data:image/jpeg;base64,...}) ou somente o conteúdo raw.
     */
    @JsonProperty("selfie_base64")
    private String selfieBase64;
}
