package com.engss.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes_pix")
public class TransacaoPix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave_pix_destino", nullable = false)
    private String chavePixDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransacao status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public TransacaoPix() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusTransacao.PENDENTE;
    }

    public TransacaoPix(String chavePixDestino, BigDecimal valor, String descricao, Usuario usuario) {
        this.chavePixDestino = chavePixDestino;
        this.valor = valor;
        this.descricao = descricao;
        this.usuario = usuario;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusTransacao.PENDENTE;
    }

    public Long getId() {
        return id;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}