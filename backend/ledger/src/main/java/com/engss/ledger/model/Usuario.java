package com.engss.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<TransacaoPix> transacoes = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String nome, BigDecimal saldo) {
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

    public List<TransacaoPix> getTransacoes() {
        return transacoes;
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

    public void setTransacoes(List<TransacaoPix> transacoes) {
        this.transacoes = transacoes;
    }

    public void debitar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor para débito deve ser maior que zero.");
        }

        if (this.saldo.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor para crédito deve ser maior que zero.");
        }

        this.saldo = this.saldo.add(valor);
    }

    public void adicionarTransacao(TransacaoPix transacao) {
        this.transacoes.add(transacao);
        transacao.setUsuario(this);
    }
}