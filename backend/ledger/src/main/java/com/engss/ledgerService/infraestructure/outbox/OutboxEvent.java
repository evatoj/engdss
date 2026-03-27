package com.engss.ledgerService.infraestructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String routingKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean published = false;

    private OutboxEvent() {}

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getRoutingKey() { return routingKey; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }

    public static OutboxEvent of(String eventType, String payload, String routingKey) {
        var e = new OutboxEvent();
        e.eventType = eventType;
        e.payload = payload;
        e.routingKey = routingKey;
        e.createdAt = Instant.now();
        return e;
    }

    public void markPublished() {
        this.published = true;
    }
}