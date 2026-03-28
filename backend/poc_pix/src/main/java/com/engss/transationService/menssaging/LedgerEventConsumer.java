package com.engss.transationService.messaging;

import com.engss.transationService.model.StatusTransacao;
import com.engss.transationService.model.Usuario;
import com.engss.transationService.repository.TransacaoPixRepository;
import com.engss.transationService.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventConsumer.class);

    private final TransacaoPixRepository transacaoPixRepository;
    private final UsuarioRepository usuarioRepository;

    public LedgerEventConsumer(TransacaoPixRepository transacaoPixRepository,
                                UsuarioRepository usuarioRepository) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @RabbitListener(queues = "transaction.ledger.debited")
    @Transactional
    public void onLedgerDebited(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.CONCLUIDA);
                transacaoPixRepository.save(transacao);

                Usuario usuario = transacao.getUsuario();
                usuario.concluirDebitoPendente(transacao.getValor());
                usuarioRepository.save(usuario);

                log.info("Transacao CONCLUIDA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebited. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "transaction.ledger.debit.confirmed")
    @Transactional
    public void onLedgerDebitConfirmed(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();
            log.info("LedgerDebitConfirmed recebido. correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebitConfirmed. payload={}", payload, e);
        }
    }

    @RabbitListener(queues = "transaction.ledger.reversed")
    @Transactional
    public void onLedgerReversed(Map<String, Object> payload) {
        try {
            String correlationId = payload.get("correlationId").toString();

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.FALHA);
                transacaoPixRepository.save(transacao);

                Usuario usuario = transacao.getUsuario();
                usuario.estornarSaldoPendente(transacao.getValor());
                usuarioRepository.save(usuario);

                log.info("Transacao REVERTIDA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerReversed. payload={}", payload, e);
            throw new RuntimeException(e);
        }
    }
}