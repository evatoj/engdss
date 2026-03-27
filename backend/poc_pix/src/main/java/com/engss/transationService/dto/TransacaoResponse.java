package com.engss.transationService.dto;

import com.engss.transationService.model.StatusTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoResponse {

    private Long id;
    private Long usuarioId;
    private String chavePixDestino;
    private BigDecimal valor;
    private String descricao;
    private StatusTransacao status;
    private LocalDateTime dataCriacao;

    public TransacaoResponse() {
    }

    public TransacaoResponse(Long id,
                             Long usuarioId,
                             String chavePixDestino,
                             BigDecimal valor,
                             String descricao,
                             StatusTransacao status,
                             LocalDateTime dataCriacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.chavePixDestino = chavePixDestino;
        this.valor = valor;
        this.descricao = descricao;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setChavePixDestino(String chavePixDestino) {
        this.chavePixDestino = chavePixDestino;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}