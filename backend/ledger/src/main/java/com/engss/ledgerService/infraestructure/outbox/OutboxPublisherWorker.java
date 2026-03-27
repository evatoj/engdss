package com.engss.ledgerService.infraestructure.outbox;

import com.engss.ledgerService.infraestructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherWorker.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

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
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    event.getRoutingKey(),
                    event.getPayload()
                );
                event.markPublished();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Falha ao publicar outbox event id={}. Será retentado.", event.getId(), e);
            }
        }
    }
}