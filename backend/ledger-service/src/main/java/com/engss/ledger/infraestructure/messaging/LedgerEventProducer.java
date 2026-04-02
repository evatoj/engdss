package com.engss.ledger.infraestructure.messaging;

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
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;

    public void publishLedgerDebited(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_DEBITED, Map.of(
            "accountId", accountId,
            "amount", amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerDebited. correlationId={}", correlationId);
    }

    public void publishLedgerDebitConfirmed(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_DEBIT_CONFIRMED, Map.of(
            "accountId", accountId,
            "amount", amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerDebitConfirmed. correlationId={}", correlationId);
    }

    public void publishLedgerReversed(UUID accountId, BigDecimal amount, UUID correlationId) {
        send(RabbitMQConfig.RK_LEDGER_REVERSED, Map.of(
            "accountId", accountId,
            "amount", amount,
            "correlationId", correlationId
        ));
        log.info("Publicado LedgerReversed. correlationId={}", correlationId);
    }

    @SneakyThrows
    private void send(String routingKey, Map<String, Object> payload) {
        Tracer tracer = openTelemetry.getTracer("ledger-service", "1.0.0");

        Span span = tracer.spanBuilder("rabbitmq publish " + routingKey)
            .setSpanKind(SpanKind.PRODUCER)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "rabbitmq");
            span.setAttribute("messaging.destination.name", RabbitMQConfig.EXCHANGE);
            span.setAttribute("messaging.rabbitmq.destination.routing_key", routingKey);
            span.setAttribute("messaging.operation", "publish");

            String body = objectMapper.writeValueAsString(payload);

            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(io.opentelemetry.context.Context.current(), props, MESSAGE_PROPERTIES_SETTER);

            Message message = MessageBuilder
                .withBody(body.getBytes(StandardCharsets.UTF_8))
                .andProperties(props)
                .build();

            rabbitTemplate.send(RabbitMQConfig.EXCHANGE, routingKey, message);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
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