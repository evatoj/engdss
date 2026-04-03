package com.engss.transaction.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class UsuarioResponse {

    private UUID id;
    private String nome;
    private BigDecimal saldo;

    public UsuarioResponse(UUID id, String nome, BigDecimal saldo) {
        this.id = id;
        this.nome = nome;
        this.saldo = saldo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}