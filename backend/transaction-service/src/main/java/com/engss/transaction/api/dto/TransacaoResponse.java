package com.engss.transaction.api.dto;

import com.engss.transaction.domain.model.StatusTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransacaoResponse {

    private UUID id;
    private UUID usuarioId;
    private String chavePixDestino;
    private BigDecimal valor;
    private String descricao;
    private StatusTransacao status;
    private LocalDateTime dataCriacao;
    // private String asaasTransferId;
    // private String asaasStatus;
    // private String motivoFalha;

    public TransacaoResponse(UUID id,
                             UUID usuarioId,
                             String chavePixDestino,
                             BigDecimal valor,
                             String descricao,
                             StatusTransacao status,
                             LocalDateTime dataCriacao
                            // String asaasTransferId,
                            // String asaasStatus,
                            //  String motivoFalha
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.chavePixDestino = chavePixDestino;
        this.valor = valor;
        this.descricao = descricao;
        this.status = status;
        this.dataCriacao = dataCriacao;
        // this.asaasTransferId = asaasTransferId;
        // this.asaasStatus = asaasStatus;
        // this.motivoFalha = motivoFalha;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getChavePixDestino() {
        return chavePixDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    // public String getAsaasTransferId() {
    //     return asaasTransferId;
    // }

    // public String getAsaasStatus() {
    //     return asaasStatus;
    // }

    // public String getMotivoFalha() {
    //     return motivoFalha;
    // }
}
