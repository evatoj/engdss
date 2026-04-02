package com.engss.transaction.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class AsaasConfig {

    @Bean
    public RestClient asaasRestClient(@Value("${asaas.base-url}") String baseUrl,
                                      @Value("${asaas.api-key:}") String apiKey) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader("access_token", apiKey)
                .build();
    }
}
