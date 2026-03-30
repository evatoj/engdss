package com.engss.transaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CriarTransacaoRequest {

    @NotNull(message = "O id do usuário é obrigatório.")
    private Long usuarioId;

    @NotBlank(message = "A chave PIX de destino é obrigatória.")
    private String chavePixDestino;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor da transação deve ser maior que zero.")
    private BigDecimal valor;

    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    public CriarTransacaoRequest() {
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
}