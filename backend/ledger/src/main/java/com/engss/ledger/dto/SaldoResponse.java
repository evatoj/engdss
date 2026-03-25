package com.engss.ledger.dto;

import java.math.BigDecimal;

public class SaldoResponse {

    private Long usuarioId;
    private String nome;
    private BigDecimal saldo;

    public SaldoResponse() {
    }

    public SaldoResponse(Long usuarioId, String nome, BigDecimal saldo) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.saldo = saldo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}