package com.engss.transationService.messaging;

import com.engss.transationService.config.RabbitMQConfig;
import com.engss.transationService.model.StatusTransacao;
import com.engss.transationService.model.TransacaoPix;
import com.engss.transationService.model.Usuario;
import com.engss.transationService.repository.TransacaoPixRepository;
import com.engss.transationService.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class LedgerEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LedgerEventConsumer.class);

    private final TransacaoPixRepository transacaoPixRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public LedgerEventConsumer(TransacaoPixRepository transacaoPixRepository,
                                UsuarioRepository usuarioRepository,
                                ObjectMapper objectMapper) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    // Ledger confirmou o débito — PIX foi debitado com sucesso
    @RabbitListener(queues = RabbitMQConfig.Q_LEDGER_DEBITED)
    @Transactional
    public void onLedgerDebited(String message) {
        try {
            var payload = parse(message);
            var correlationId = UUID.fromString(payload.get("correlationId").toString());

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.CONCLUIDA);
                transacaoPixRepository.save(transacao);

                // confirma débito pendente no saldo do usuário
                Usuario usuario = transacao.getUsuario();
                usuario.concluirDebitoPendente(transacao.getValor());
                usuarioRepository.save(usuario);

                log.info("Transacao CONCLUIDA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebited. message={}", message, e);
            throw new RuntimeException(e);
        }
    }

    // Ledger confirmou o débito final
    @RabbitListener(queues = RabbitMQConfig.Q_LEDGER_DEBIT_CONFIRMED)
    @Transactional
    public void onLedgerDebitConfirmed(String message) {
        try {
            var payload = parse(message);
            var correlationId = UUID.fromString(payload.get("correlationId").toString());
            log.info("LedgerDebitConfirmed recebido. correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("Erro ao processar LedgerDebitConfirmed. message={}", message, e);
        }
    }

    // Ledger fez reversão — PIX falhou
    @RabbitListener(queues = RabbitMQConfig.Q_LEDGER_REVERSED)
    @Transactional
    public void onLedgerReversed(String message) {
        try {
            var payload = parse(message);
            var correlationId = UUID.fromString(payload.get("correlationId").toString());

            transacaoPixRepository.findByCorrelationId(correlationId).ifPresent(transacao -> {
                transacao.setStatus(StatusTransacao.FALHA);
                transacaoPixRepository.save(transacao);

                // estorna o saldo pendente de volta ao disponível
                Usuario usuario = transacao.getUsuario();
                usuario.estornarSaldoPendente(transacao.getValor());
                usuarioRepository.save(usuario);

                log.info("Transacao REVERTIDA. correlationId={}", correlationId);
            });
        } catch (Exception e) {
            log.error("Erro ao processar LedgerReversed. message={}", message, e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String message) throws Exception {
        return objectMapper.readValue(message, Map.class);
    }
}