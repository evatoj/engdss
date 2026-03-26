package com.engss.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "saldo_disponivel", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponivel;

    @Column(name = "saldo_pendente", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoPendente;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<TransacaoPix> transacoes = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String nome, BigDecimal saldoDisponivel) {
        this.nome = nome;
        this.saldoDisponivel = saldoDisponivel;
        this.saldoPendente = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldoDisponivel() {
        return saldoDisponivel;
    }

    public BigDecimal getSaldoPendente() {
        return saldoPendente;
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

    public void setSaldoDisponivel(BigDecimal saldoDisponivel) {
        this.saldoDisponivel = saldoDisponivel;
    }

    public void setSaldoPendente(BigDecimal saldoPendente) {
        this.saldoPendente = saldoPendente;
    }

    public void setTransacoes(List<TransacaoPix> transacoes) {
        this.transacoes = transacoes;
    }

    public void reservarSaldo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        if (this.saldoDisponivel.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        this.saldoDisponivel = this.saldoDisponivel.subtract(valor);
        this.saldoPendente = this.saldoPendente.add(valor);
    }

    public void concluirDebitoPendente(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        if (this.saldoPendente.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo pendente insuficiente.");
        }

        this.saldoPendente = this.saldoPendente.subtract(valor);
    }

    public void estornarSaldoPendente(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        if (this.saldoPendente.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo pendente insuficiente.");
        }

        this.saldoPendente = this.saldoPendente.subtract(valor);
        this.saldoDisponivel = this.saldoDisponivel.add(valor);
    }

    public void creditar(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        this.saldoDisponivel = this.saldoDisponivel.add(valor);
    }

    public void adicionarTransacao(TransacaoPix transacao) {
        this.transacoes.add(transacao);
        transacao.setUsuario(this);
    }
}