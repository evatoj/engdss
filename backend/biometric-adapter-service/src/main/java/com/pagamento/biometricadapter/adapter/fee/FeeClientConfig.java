package com.pagamento.biometricadapter.adapter.fee;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuração do {@link WebClient} dedicado à Validra API.
 *
 * <p>Propriedades relevantes em {@code application.yml}:
 * <pre>
 * validra:
 *   base-url: https://api.validra.com.br
 *   api-key: ${VALIDRA_API_KEY}
 *   timeout:
 *     connect-ms: 3000
 *     read-ms: 10000
 *     write-ms: 5000
 *   retry:
 *     max-attempts: 2
 *     back-off-ms: 500
 * </pre>
 */
@Configuration
public class FeeClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeeClientConfig.class);

    /** Header de autenticação exigido pela Validra. */
    public static final String API_KEY_HEADER = "X-API-Key";

    @Value("${validra.base-url:https://api.validra.com.br}")
    private String baseUrl;

    @Value("${validra.api-key:STUB_KEY}")
    private String apiKey;

    @Value("${validra.timeout.connect-ms:3000}")
    private int connectTimeoutMs;

    @Value("${validra.timeout.read-ms:10000}")
    private int readTimeoutMs;

    @Value("${validra.timeout.write-ms:5000}")
    private int writeTimeoutMs;

    @Bean("validraWebClient")
    public WebClient validraWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(API_KEY_HEADER, apiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Política de retry reativa — usada pelo {@link FeeClient} via
     * {@link Mono#retryWhen(Retry)}.
     * Exponencial com jitter, apenas para erros 5xx/timeout (não para 4xx).
     */
    @Bean("validraRetrySpec")
    public Retry validraRetrySpec(
            @Value("${validra.retry.max-attempts:2}") long maxAttempts,
            @Value("${validra.retry.back-off-ms:500}") long backOffMs) {

        return Retry.backoff(maxAttempts, Duration.ofMillis(backOffMs))
                .jitter(0.3)
                .filter(ex -> !(ex instanceof FeeClientException fce) || fce.isServerError())
                .doBeforeRetry(signal ->
                        log.warn("[Validra] Retry attempt {} after error: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage()));
    }

    // ------------------------------------------------------------------ //
    //  Logging filters                                                      //
    // ------------------------------------------------------------------ //

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("[Validra] → {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.debug("[Validra] ← HTTP {}", res.statusCode().value());
            return Mono.just(res);
        });
    }
}
