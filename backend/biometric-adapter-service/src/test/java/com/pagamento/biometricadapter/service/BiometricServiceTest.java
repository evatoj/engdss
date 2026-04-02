package com.pagamento.biometricadapter.service;

import com.pagamento.biometricadapter.adapter.fee.FeeClient;
import com.pagamento.biometricadapter.adapter.fee.FeeClientException;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyRequest;
import com.pagamento.biometricadapter.adapter.fee.dto.FeeVerifyResponse;
import com.pagamento.biometricadapter.domain.BiometricRequest;
import com.pagamento.biometricadapter.domain.BiometricResponse;
import com.pagamento.biometricadapter.domain.BiometricStatus;
import com.pagamento.biometricadapter.messaging.BiometricResponsePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiometricServiceTest {

    @Mock FeeClient feeClient;
    @Mock BiometricResponsePublisher publisher;

    @InjectMocks BiometricService service;

    private BiometricRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = BiometricRequest.builder()
                .transactionId(UUID.randomUUID())
                .cpf("12345678901")
                .selfieBase64("base64encodedimage==")
                .requestedAt(Instant.now())
                .attempt(1)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  Cenário: APPROVED                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Deve publicar APPROVED quando Validra retorna match com score alto")
    void shouldPublishApprovedWhenValidraReturnsMatch() {
        FeeVerifyResponse feeResponse = buildFeeResponse("approved", 0.98, null);

        when(feeClient.verify(any(FeeVerifyRequest.class))).thenReturn(Mono.just(feeResponse));
        when(feeClient.mapStatus(feeResponse)).thenReturn(BiometricStatus.APPROVED);

        service.process(validRequest);

        ArgumentCaptor<BiometricResponse> captor = ArgumentCaptor.forClass(BiometricResponse.class);
        verify(publisher).publish(captor.capture());

        BiometricResponse published = captor.getValue();
        assertThat(published.getTransactionId()).isEqualTo(validRequest.getTransactionId());
        assertThat(published.getStatus()).isEqualTo(BiometricStatus.APPROVED);
        assertThat(published.getSimilarityScore()).isEqualTo(0.98);
    }

    // ------------------------------------------------------------------ //
    //  Cenário: REJECTED                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Deve publicar REJECTED quando Validra indica face_mismatch")
    void shouldPublishRejectedWhenValidraReturnsMismatch() {
        FeeVerifyResponse feeResponse = buildFeeResponse("rejected", 0.42, "face_mismatch");

        when(feeClient.verify(any(FeeVerifyRequest.class))).thenReturn(Mono.just(feeResponse));
        when(feeClient.mapStatus(feeResponse)).thenReturn(BiometricStatus.REJECTED);

        service.process(validRequest);

        ArgumentCaptor<BiometricResponse> captor = ArgumentCaptor.forClass(BiometricResponse.class);
        verify(publisher).publish(captor.capture());

        BiometricResponse published = captor.getValue();
        assertThat(published.getStatus()).isEqualTo(BiometricStatus.REJECTED);
        assertThat(published.getMessage()).isEqualTo("face_mismatch");
        assertThat(published.getSimilarityScore()).isEqualTo(0.42);
    }

    // ------------------------------------------------------------------ //
    //  Cenário: ERROR por FeeClientException 5xx                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Deve publicar ERROR quando Validra retorna erro 503")
    void shouldPublishErrorOnValidraServerError() {
        when(feeClient.verify(any(FeeVerifyRequest.class)))
                .thenReturn(Mono.error(new FeeClientException("Service Unavailable",
                        HttpStatus.SERVICE_UNAVAILABLE)));

        service.process(validRequest);

        ArgumentCaptor<BiometricResponse> captor = ArgumentCaptor.forClass(BiometricResponse.class);
        verify(publisher).publish(captor.capture());

        BiometricResponse published = captor.getValue();
        assertThat(published.getStatus()).isEqualTo(BiometricStatus.ERROR);
        assertThat(published.getMessage()).isEqualTo("provider_server_error_503");
        assertThat(published.getSimilarityScore()).isNull();
    }

    // ------------------------------------------------------------------ //
    //  Cenário: ERROR por FeeClientException 4xx                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Deve publicar ERROR quando Validra retorna 401 (API Key inválida)")
    void shouldPublishErrorOnValidraClientError() {
        when(feeClient.verify(any(FeeVerifyRequest.class)))
                .thenReturn(Mono.error(new FeeClientException("Unauthorized",
                        HttpStatus.UNAUTHORIZED)));

        service.process(validRequest);

        ArgumentCaptor<BiometricResponse> captor = ArgumentCaptor.forClass(BiometricResponse.class);
        verify(publisher).publish(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(BiometricStatus.ERROR);
        assertThat(captor.getValue().getMessage()).isEqualTo("provider_client_error_401");
    }

    // ------------------------------------------------------------------ //
    //  Cenário: ERROR por exceção inesperada                               //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Deve publicar ERROR e não propagar exceção inesperada")
    void shouldPublishErrorOnUnexpectedException() {
        when(feeClient.verify(any(FeeVerifyRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("NullPointerException inesperada")));

        // Não deve lançar exceção — o consumer não deve gerar NACK
        service.process(validRequest);

        ArgumentCaptor<BiometricResponse> captor = ArgumentCaptor.forClass(BiometricResponse.class);
        verify(publisher).publish(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(BiometricStatus.ERROR);
        assertThat(captor.getValue().getMessage()).isEqualTo("internal_error");
    }

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    private FeeVerifyResponse buildFeeResponse(String status, Double score, String reason) {
        FeeVerifyResponse r = new FeeVerifyResponse();
        r.setStatus(status);
        r.setSimilarityScore(score);
        r.setReason(reason);
        r.setRequestId(UUID.randomUUID().toString());
        return r;
    }
}
