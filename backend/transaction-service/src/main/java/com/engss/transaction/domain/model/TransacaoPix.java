package com.engss.transaction.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacao_pix")
public class TransacaoPix {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

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

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "referencia_externa", length = 100, unique = true)
    private String correlationId;

    @Column(name = "chave_idempotencia", length = 100, nullable = false, unique = true)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public TransacaoPix() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.status = StatusTransacao.PENDENTE;
    }

    public UUID getId() {
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

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public UUID getCorrelationId() {
        return correlationId != null ? UUID.fromString(correlationId) : null;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey != null ? UUID.fromString(idempotencyKey) : null;
    }

    public void setId(UUID id) {
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
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId != null ? correlationId.toString() : null;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey != null ? idempotencyKey.toString() : null;
    }
}