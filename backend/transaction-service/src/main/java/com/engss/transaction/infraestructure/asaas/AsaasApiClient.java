package com.engss.transaction.infraestructure.asaas;    

import com.engss.transaction.infraestructure.asaas.dto.AsaasTransferRequest;
import com.engss.transaction.infraestructure.asaas.dto.AsaasTransferResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AsaasApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public AsaasApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${asaas.base-url}") String baseUrl,
            @Value("${asaas.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    public AsaasTransferResponse criarTransferencia(AsaasTransferRequest request) {
        try {
            AsaasTransferResponse response = restClient.post()
                    .uri("/api/v3/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("access_token", apiKey)
                    .body(request)
                    .retrieve()
                    .body(AsaasTransferResponse.class);

            if (response == null) {
                throw new IllegalStateException("Resposta nula ao criar transferência no Asaas.");
            }

            return response;
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            throw new IllegalStateException(
                    "Erro ao criar transferência no Asaas. HTTP " + e.getStatusCode() + " - " + body,
                    e
            );
        }
    }
}