package com.engss.transaction.infraestructure.pix;

import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.infraestructure.asaas.AsaasApiClient;
import com.engss.transaction.infraestructure.asaas.dto.AsaasTransferRequest;
import com.engss.transaction.infraestructure.asaas.dto.AsaasTransferResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pix.provider", havingValue = "asaas")
public class AsaasPixAdapter implements PixAdapter {

    private final AsaasApiClient asaasApiClient;

    public AsaasPixAdapter(AsaasApiClient asaasApiClient) {
        this.asaasApiClient = asaasApiClient;
    }

    @Override
    public PixTransferResult transferir(TransacaoPix transacao) {
        AsaasTransferRequest request = new AsaasTransferRequest(
                transacao.getValor(),
                "PIX",
                transacao.getChavePixDestino(),
                transacao.getDescricao(),
                transacao.getCorrelationId().toString()
        );

        AsaasTransferResponse response = asaasApiClient.criarTransferencia(request);

        String transferId = response.getId();
        String status = response.getStatus();
        String motivoFalha = response.getFailReason();

        if ("DONE".equalsIgnoreCase(status)) {
            return PixTransferResult.sucesso(transferId, status);
        }

        if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            return PixTransferResult.falha(transferId, status, motivoFalha);
        }

        return PixTransferResult.falha(
                transferId,
                status,
                motivoFalha != null ? motivoFalha : "Status inesperado retornado pelo Asaas."
        );
    }
}