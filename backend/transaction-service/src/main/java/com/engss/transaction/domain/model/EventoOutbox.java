package com.engss.transaction.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "evento_outbox")
public class EventoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transacao_pix_id")
    private UUID transacaoPixId;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Integer tentativas;

    @Column(name = "erro_ultimo_envio")
    private String erroUltimoEnvio;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    public EventoOutbox() {
        this.status = "PENDENTE";
        this.tentativas = 0;
        this.dataCriacao = LocalDateTime.now();
    }

    public static EventoOutbox criar(UUID transacaoPixId,
                                     String tipoEvento,
                                     Map<String, Object> payload,
                                     String routingKey) {
        EventoOutbox evento = new EventoOutbox();
        evento.transacaoPixId = transacaoPixId;
        evento.tipoEvento = tipoEvento;
        evento.payload = payload;
        evento.routingKey = routingKey;
        return evento;
    }

    public void marcarComoPublicado() {
        this.status = "PUBLICADO";
        this.dataPublicacao = LocalDateTime.now();
        this.erroUltimoEnvio = null;
    }

    public void registrarFalha(String erro) {
        this.tentativas = this.tentativas + 1;
        this.erroUltimoEnvio = erro;
        this.status = "PENDENTE";
    }

    public Long getId() { return id; }
    public UUID getTransacaoPixId() { return transacaoPixId; }
    public String getTipoEvento() { return tipoEvento; }
    public Map<String, Object> getPayload() { return payload; }
    public String getRoutingKey() { return routingKey; }
    public String getStatus() { return status; }
    public Integer getTentativas() { return tentativas; }
    public String getErroUltimoEnvio() { return erroUltimoEnvio; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataPublicacao() { return dataPublicacao; }
}