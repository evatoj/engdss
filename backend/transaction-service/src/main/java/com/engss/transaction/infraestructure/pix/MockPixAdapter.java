package com.engss.transaction.infraestructure.pix;

import com.engss.transaction.domain.model.TransacaoPix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "pix.provider", havingValue = "mock", matchIfMissing = true)
public class MockPixAdapter implements PixAdapter {

    private final String mockStatus;
    private final String mockFailReason;
    private final long mockDelayMs;

    public MockPixAdapter(
            @Value("${pix.mock.status:DONE}") String mockStatus,
            @Value("${pix.mock.fail-reason:Falha simulada do provedor PIX}") String mockFailReason,
            @Value("${pix.mock.delay-ms:0}") long mockDelayMs
    ) {
        this.mockStatus = mockStatus;
        this.mockFailReason = mockFailReason;
        this.mockDelayMs = mockDelayMs;
    }

    @Override
    public PixTransferResult transferir(TransacaoPix transacao) {
        aplicarAtrasoSeConfigurado();

        String statusNormalizado = mockStatus == null
                ? "DONE"
                : mockStatus.trim().toUpperCase(Locale.ROOT);

        String transferId = "mock-" + UUID.randomUUID();

        return switch (statusNormalizado) {
            case "DONE" -> PixTransferResult.sucesso(transferId, "DONE");
            case "FAILED" -> PixTransferResult.falha(transferId, "FAILED", mockFailReason);
            case "CANCELLED" -> PixTransferResult.falha(transferId, "CANCELLED", mockFailReason);
            default -> throw new IllegalArgumentException(
                    "Valor inválido para pix.mock.status: " + mockStatus +
                    ". Use DONE, FAILED ou CANCELLED."
            );
        };
    }

    private void aplicarAtrasoSeConfigurado() {
        if (mockDelayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(mockDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrompida durante atraso do mock PIX.", e);
        }
    }
}