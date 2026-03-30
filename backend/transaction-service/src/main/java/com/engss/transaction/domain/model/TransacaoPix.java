package com.engss.transaction.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacao_pix")
public class TransacaoPix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave_pix_destino", nullable = false)
    private String chavePixDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTransacao status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "referencia_externa", length = 100)
    private String correlationId; // armazena como String para compatibilidade com o schema

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

    public Long getId() { return id; }
    public String getChavePixDestino() { return chavePixDestino; }
    public BigDecimal getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public StatusTransacao getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Usuario getUsuario() { return usuario; }

    public UUID getCorrelationId() {
        return correlationId != null ? UUID.fromString(correlationId) : null;
    }

    public void setId(Long id) { this.id = id; }
    public void setChavePixDestino(String chavePixDestino) { this.chavePixDestino = chavePixDestino; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(StatusTransacao status) { this.status = status; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId != null ? correlationId.toString() : null;
    }
}