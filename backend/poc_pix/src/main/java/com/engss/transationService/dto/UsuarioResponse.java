package com.engss.transationService.dto;

import java.math.BigDecimal;

public class UsuarioResponse {

    private Long id;
    private String nome;
    private BigDecimal saldo;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Long id, String nome, BigDecimal saldo) {
        this.id = id;
        this.nome = nome;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}