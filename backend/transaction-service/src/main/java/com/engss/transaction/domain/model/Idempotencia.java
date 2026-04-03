package com.engss.transaction.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "idempotencia")
public class Idempotencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave", nullable = false, unique = true, length = 100)
    private String chave;

    @Column(name = "transacao_pix_id")
    private UUID transacaoPixId;

    @Column(name = "requisicao_hash", length = 255)
    private String requisicaoHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resposta", columnDefinition = "jsonb")
    private Map<String, Object> resposta;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    public Idempotencia() {
        this.dataCriacao = LocalDateTime.now();
        this.status = "PROCESSANDO";
    }

    public static Idempotencia iniciar(String chave, String requisicaoHash) {
        Idempotencia id = new Idempotencia();
        id.chave = chave;
        id.requisicaoHash = requisicaoHash;
        id.status = "PROCESSANDO";
        return id;
    }

    public void concluir(UUID transacaoPixId) {
        this.transacaoPixId = transacaoPixId;
        this.status = "CONCLUIDA";
    }

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public UUID getTransacaoPixId() {
        return transacaoPixId;
    }

    public String getRequisicaoHash() {
        return requisicaoHash;
    }

    public Map<String, Object> getResposta() {
        return resposta;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public void setTransacaoPixId(UUID transacaoPixId) {
        this.transacaoPixId = transacaoPixId;
    }

    public void setRequisicaoHash(String requisicaoHash) {
        this.requisicaoHash = requisicaoHash;
    }

    public void setResposta(Map<String, Object> resposta) {
        this.resposta = resposta;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}