package com.engss.transaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CriarUsuarioRequest {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotNull(message = "O saldo inicial é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "O saldo inicial não pode ser negativo.")
    private BigDecimal saldoInicial;

    public CriarUsuarioRequest() {
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }
}