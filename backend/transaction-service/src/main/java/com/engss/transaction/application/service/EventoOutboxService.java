package com.engss.transaction.application.service;

import com.engss.transaction.domain.model.EventoOutbox;
import com.engss.transaction.domain.repository.EventoOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventoOutboxService {

    private final EventoOutboxRepository eventoOutboxRepository;

    @Value("${app.rabbit.routing-key.credito-inicial}")
    private String rkCreditoInicial;

    @Value("${app.rabbit.routing-key.saque-iniciado}")
    private String rkSaqueIniciado;

    public EventoOutboxService(EventoOutboxRepository eventoOutboxRepository) {
        this.eventoOutboxRepository = eventoOutboxRepository;
    }

    public void registrarCreditoInicial(UUID accountId,
                                        UUID idempotencyKey,
                                        BigDecimal amount,
                                        UUID correlationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accountId", accountId.toString());
        payload.put("idempotencyKey", idempotencyKey.toString());
        payload.put("amount", amount);
        payload.put("correlationId", correlationId.toString());

        EventoOutbox evento = EventoOutbox.criar(
                null,
                "CreditoInicial",
                payload,
                rkCreditoInicial
        );

        eventoOutboxRepository.save(evento);
    }

    public void registrarSaqueIniciado(UUID transacaoPixId,
                                       UUID accountId,
                                       UUID idempotencyKey,
                                       BigDecimal amount,
                                       UUID correlationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accountId", accountId.toString());
        payload.put("idempotencyKey", idempotencyKey.toString());
        payload.put("amount", amount);
        payload.put("correlationId", correlationId.toString());

        EventoOutbox evento = EventoOutbox.criar(
                transacaoPixId,
                "SaqueIniciado",
                payload,
                rkSaqueIniciado
        );

        eventoOutboxRepository.save(evento);
    }
}