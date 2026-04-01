package com.engss.transaction.infraestructure.outbox;

import com.engss.transaction.domain.model.EventoOutbox;
import com.engss.transaction.domain.repository.EventoOutboxRepository;
import com.engss.transaction.infraestructure.messaging.OutboxRabbitPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisherWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherWorker.class);

    private final EventoOutboxRepository eventoOutboxRepository;
    private final OutboxRabbitPublisher outboxRabbitPublisher;

    public OutboxPublisherWorker(EventoOutboxRepository eventoOutboxRepository,
                                 OutboxRabbitPublisher outboxRabbitPublisher) {
        this.eventoOutboxRepository = eventoOutboxRepository;
        this.outboxRabbitPublisher = outboxRabbitPublisher;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publicarPendentes() {
        List<EventoOutbox> pendentes =
                eventoOutboxRepository.findTop20ByStatusOrderByDataCriacaoAsc("PENDENTE");

        for (EventoOutbox evento : pendentes) {
            try {
                outboxRabbitPublisher.publish(evento.getRoutingKey(), evento.getPayload());
                evento.marcarComoPublicado();
                eventoOutboxRepository.save(evento);
                log.info("Evento outbox publicado. id={}, tipo={}", evento.getId(), evento.getTipoEvento());
            } catch (Exception e) {
                evento.registrarFalha(e.getMessage());
                eventoOutboxRepository.save(evento);
                log.error("Erro ao publicar evento outbox. id={}", evento.getId(), e);
            }
        }
    }
}