package com.engss.ledger.infraestructure.messaging;

import com.engss.ledger.application.command.CreditCommand;
import com.engss.ledger.application.command.DebitCommand;
import com.engss.ledger.application.command.ReversalCommand;
import com.engss.ledger.application.service.LedgerCommandService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventConsumer.class);

    private final LedgerCommandService ledgerCommandService;
    private final OpenTelemetry openTelemetry;

    @RabbitListener(queues = "${ledger.queue.saque-iniciado}")
    public void onSaqueIniciado(Map<String, Object> payload, Message amqpMessage) {
        consume("ledger.saque.iniciado", payload, amqpMessage, () -> {
            var cmd = new DebitCommand(
                UUID.fromString(payload.get("accountId").toString()),
                UUID.fromString(payload.get("idempotencyKey").toString()),
                new BigDecimal(payload.get("amount").toString()),
                UUID.fromString(payload.get("correlationId").toString())
            );
            ledgerCommandService.debitPending(cmd);
            log.info("DEBIT_PENDING aplicado. correlationId={}", cmd.correlationId());
        });
    }

    @RabbitListener(queues = "${ledger.queue.pix-confirmado}")
    public void onPixConfirmado(Map<String, Object> payload, Message amqpMessage) {
        consume("ledger.pix.confirmado", payload, amqpMessage, () -> {
            var correlationId = UUID.fromString(payload.get("correlationId").toString());
            var accountId = UUID.fromString(payload.get("accountId").toString());
            var amount = new BigDecimal(payload.get("amount").toString());

            ledgerCommandService.confirmDebit(correlationId, accountId, amount);
            log.info("DEBIT_CONFIRMED aplicado. correlationId={}", correlationId);
        });
    }

    @RabbitListener(queues = "${ledger.queue.pix-falhou}")
    public void onPixFalhou(Map<String, Object> payload, Message amqpMessage) {
        consume("ledger.pix.falhou", payload, amqpMessage, () -> {
            var cmd = new ReversalCommand(
                UUID.fromString(payload.get("accountId").toString()),
                UUID.fromString(payload.get("correlationId").toString())
            );
            ledgerCommandService.reverse(cmd);
            log.info("REVERSAL aplicado. correlationId={}", cmd.correlationId());
        });
    }

    @RabbitListener(queues = "${ledger.queue.credito-inicial}")
    public void onCreditoInicial(Map<String, Object> payload, Message amqpMessage) {
        consume("ledger.credito.inicial", payload, amqpMessage, () -> {
            var cmd = new CreditCommand(
                UUID.fromString(payload.get("accountId").toString()),
                UUID.fromString(payload.get("idempotencyKey").toString()),
                new BigDecimal(payload.get("amount").toString()),
                UUID.fromString(payload.get("correlationId").toString())
            );

            ledgerCommandService.creditInitial(cmd);
            log.info("CREDIT_INITIAL aplicado. accountId={}", cmd.accountId());
        });
    }

    private void consume(String queueName, Map<String, Object> payload, Message amqpMessage, Runnable handler) {
        Tracer tracer = openTelemetry.getTracer("ledger-service", "1.0.0");

        Context extractedContext = openTelemetry.getPropagators()
            .getTextMapPropagator()
            .extract(Context.current(), amqpMessage, MESSAGE_GETTER);

        Span span = tracer.spanBuilder("rabbitmq consume " + queueName)
            .setParent(extractedContext)
            .setSpanKind(SpanKind.CONSUMER)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "rabbitmq");
            span.setAttribute("messaging.destination.name", queueName);
            span.setAttribute("messaging.operation", "process");

            Object correlationId = payload.get("correlationId");
            if (correlationId != null) {
                span.setAttribute("messaging.message.conversation_id", correlationId.toString());
            }

            handler.run();

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            log.error("Erro ao processar mensagem da fila {}. payload={}", queueName, payload, e);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }

    private static final TextMapGetter<Message> MESSAGE_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Message carrier) {
            if (carrier == null || carrier.getMessageProperties() == null) {
                return Collections.emptyList();
            }
            return carrier.getMessageProperties().getHeaders().keySet();
        }

        @Override
        public String get(Message carrier, String key) {
            if (carrier == null || carrier.getMessageProperties() == null) {
                return null;
            }
            Object value = carrier.getMessageProperties().getHeaders().get(key);
            return value != null ? value.toString() : null;
        }
    };
}