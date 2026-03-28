package com.engss.ledgerService.infraestructure.outbox;

import com.engss.ledgerService.infraestructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherWorker.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publish() {
        List<OutboxEvent> pending = outboxEventRepository
            .findTopNByPublishedFalseOrderByCreatedAtAsc(BATCH_SIZE);

        if (pending.isEmpty()) return;

        log.debug("Publicando {} eventos do outbox", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // parseia o JSON string para Map antes de publicar — evita double serialization
                Map<?, ?> payloadMap = objectMapper.readValue(event.getPayload(), Map.class);
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    event.getRoutingKey(),
                    payloadMap
                );
                event.markPublished();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Falha ao publicar outbox event id={}. Será retentado.", event.getId(), e);
            }
        }
    }
}