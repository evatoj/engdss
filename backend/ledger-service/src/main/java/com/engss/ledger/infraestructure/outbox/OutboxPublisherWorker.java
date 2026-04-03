package com.engss.ledger.infraestructure.outbox;

import com.engss.ledger.infraestructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherWorker.class);
    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publish() {
        Tracer tracer = openTelemetry.getTracer("ledger-service", "1.0.0");

        Span batchSpan = tracer.spanBuilder("outbox publish batch")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try (Scope batchScope = batchSpan.makeCurrent()) {
            batchSpan.setAttribute("outbox.batch.size.max", BATCH_SIZE);

            List<OutboxEvent> pending = outboxEventRepository
                .findTopNByPublishedFalseOrderByCreatedAtAsc(BATCH_SIZE);

            batchSpan.setAttribute("outbox.events.found", pending.size());

            if (pending.isEmpty()) {
                return;
            }

            log.debug("Publicando {} eventos do outbox", pending.size());

            for (OutboxEvent event : pending) {
                publishSingleEvent(event, tracer);
            }

        } catch (Exception e) {
            batchSpan.recordException(e);
            batchSpan.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            batchSpan.end();
        }
    }

    private void publishSingleEvent(OutboxEvent event, Tracer tracer) {
        Span span = tracer.spanBuilder("rabbitmq publish outbox " + event.getRoutingKey())
            .setSpanKind(SpanKind.PRODUCER)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "rabbitmq");
            span.setAttribute("messaging.destination.name", RabbitMQConfig.EXCHANGE);
            span.setAttribute("messaging.rabbitmq.destination.routing_key", event.getRoutingKey());
            span.setAttribute("messaging.operation", "publish");
            span.setAttribute("outbox.event.id", String.valueOf(event.getId()));

            Map<?, ?> payloadMap = objectMapper.readValue(event.getPayload(), Map.class);
            String body = objectMapper.writeValueAsString(payloadMap);

            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(io.opentelemetry.context.Context.current(), props, MESSAGE_PROPERTIES_SETTER);

            Message message = MessageBuilder
                .withBody(body.getBytes(StandardCharsets.UTF_8))
                .andProperties(props)
                .build();

            rabbitTemplate.send(
                RabbitMQConfig.EXCHANGE,
                event.getRoutingKey(),
                message
            );

            event.markPublished();
            outboxEventRepository.save(event);

            span.setAttribute("outbox.event.published", true);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            log.error("Falha ao publicar outbox event id={}. Será retentado.", event.getId(), e);
        } finally {
            span.end();
        }
    }

    private static final TextMapSetter<MessageProperties> MESSAGE_PROPERTIES_SETTER =
        (carrier, key, value) -> {
            if (carrier != null) {
                carrier.setHeader(key, value);
            }
        };
}