package com.engss.transaction.infraestructure.asaas;

import com.engss.transaction.application.service.PixAdapter;
import com.engss.transaction.application.service.PixTransferResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AsaasPixAdapter implements PixAdapter {

    private static final Logger log = LoggerFactory.getLogger(AsaasPixAdapter.class);

    private final RestClient asaasRestClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public AsaasPixAdapter(RestClient asaasRestClient,
                           ObjectMapper objectMapper,
                           @Value("${asaas.enabled:false}") boolean enabled) {
        this.asaasRestClient = asaasRestClient;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    public PixTransferResult transferir(UUID correlationId, String chavePixDestino, BigDecimal valor, String descricao) {
        if (!enabled) {
            log.warn("Integração Asaas desabilitada. Simulando sucesso local. correlationId={}", correlationId);
            return new PixTransferResult("mock-" + correlationId, "DONE", null);
        }

        AsaasTransferRequest request = new AsaasTransferRequest(
                valor,
                "PIX",
                chavePixDestino,
                descricao,
                correlationId.toString()
        );

        try {
            AsaasTransferResponse response = asaasRestClient.post()
                    .uri("/v3/transfers")
                    .body(request)
                    .retrieve()
                    .body(AsaasTransferResponse.class);

            if (response == null) {
                throw new IllegalStateException("Resposta vazia ao criar transferência PIX no Asaas.");
            }

            return new PixTransferResult(response.id(), response.status(), response.failReason());
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(montarMensagemErro(ex.getStatusCode(), ex.getResponseBodyAsString()), ex);
        }
    }

    private String montarMensagemErro(HttpStatusCode statusCode, String responseBody) {
        try {
            AsaasErrorResponse response = objectMapper.readValue(responseBody, AsaasErrorResponse.class);
            if (response.errors() != null && !response.errors().isEmpty()) {
                return "Erro Asaas " + statusCode.value() + ": " + response.errors().get(0).description();
            }
        } catch (Exception ignored) {
            // fallback abaixo
        }
        return "Erro Asaas " + statusCode.value() + ": " + responseBody;
    }
}
