package com.engss.transaction.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class SaldoResponse {

    private UUID usuarioId;
    private String nome;
    private BigDecimal saldo;

    public SaldoResponse(UUID usuarioId, String nome, BigDecimal saldo) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.saldo = saldo;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}