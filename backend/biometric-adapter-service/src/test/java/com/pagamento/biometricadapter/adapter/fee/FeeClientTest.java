package com.pagamento.biometricadapter.adapter.fee;

import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyRequest;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyResponse;
import com.pagamento.biometricadapter.domain.BiometricStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FeeClientTest {

    private FeeClient feeClient;

    @BeforeEach
    void setUp() {
        // Injeta um WebClient mock (não usado pelo stub) e retry sem delay
        feeClient = new FeeClient(
                mock(WebClient.class),
                Retry.max(0),
                "/v1/biometrics/facial"
        );
    }

    // ------------------------------------------------------------------ //
    //  Stub behavior                                                        //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Stub deve retornar APPROVED com score 1.0 sem chamar HTTP")
    void stubShouldReturnApproved() {
        FeeVerifyRequest request = FeeVerifyRequest.builder()
                .cpf("12345678901")
                .selfieBase64("base64==")
                .build();

        Mono<FeeVerifyResponse> result = feeClient.verify(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatus()).isEqualTo("approved");
                    assertThat(response.getSimilarityScore()).isEqualTo(1.0);
                    assertThat(response.getRequestId()).startsWith("STUB-");
                })
                .verifyComplete();
    }

    // ------------------------------------------------------------------ //
    //  Status mapping                                                       //
    // ------------------------------------------------------------------ //

    @ParameterizedTest(name = "status=''{0}'' score={1} → {2}")
    @CsvSource({
            "approved,  0.95, APPROVED",
            "match,     0.80, APPROVED",
            "verified,  0.76, APPROVED",
            "approved,  0.50, REJECTED",   // score abaixo do threshold 0.75
            "rejected,  0.42, REJECTED",
            "no_match,  0.30, REJECTED",
            "mismatch,  0.10, REJECTED",
            "error,      ,    ERROR",
            "unknown,    ,    ERROR",
    })
    @DisplayName("mapStatus deve converter corretamente todos os casos")
    void mapStatusShouldCoverAllCases(String status, Double score, BiometricStatus expected) {
        FeeVerifyResponse response = new FeeVerifyResponse();
        response.setStatus(status);
        response.setSimilarityScore(score);

        assertThat(feeClient.mapStatus(response)).isEqualTo(expected);
    }

    @Test
    @DisplayName("mapStatus deve retornar ERROR quando response for null")
    void mapStatusShouldReturnErrorForNullResponse() {
        assertThat(feeClient.mapStatus(null)).isEqualTo(BiometricStatus.ERROR);
    }

    @Test
    @DisplayName("mapStatus deve retornar ERROR quando status for null")
    void mapStatusShouldReturnErrorForNullStatus() {
        FeeVerifyResponse response = new FeeVerifyResponse();
        response.setStatus(null);

        assertThat(feeClient.mapStatus(response)).isEqualTo(BiometricStatus.ERROR);
    }
}
