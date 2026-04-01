package com.engss.transaction.application.service;

import com.engss.transaction.application.exception.RecursoNaoEncontradoException;
import com.engss.transaction.domain.model.StatusTransacao;
import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.domain.model.Usuario;
import com.engss.transaction.domain.repository.TransacaoPixRepository;
import com.engss.transaction.domain.repository.UsuarioRepository;
//import com.engss.transaction.infraestructure.messaging.PixEventPublisher;
import com.engss.transaction.application.service.EventoOutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransacaoPixService {

    private final TransacaoPixRepository transacaoPixRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoOutboxService eventoOutboxService;

    public TransacaoPixService(TransacaoPixRepository transacaoPixRepository,
                               UsuarioRepository usuarioRepository,
                               EventoOutboxService pixEventPublisher) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoOutboxService = pixEventPublisher;
    }

    @Transactional
    public TransacaoPix criarTransacao(UUID usuarioId,
                                       String chavePixDestino,
                                       BigDecimal valor,
                                       String descricao,
                                       String idempotencyKeyHeader) {
        if (chavePixDestino == null || chavePixDestino.isBlank()) {
            throw new IllegalArgumentException("A chave PIX de destino é obrigatória.");
        }

        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        UUID idempotencyKey = parseOrGenerateIdempotencyKey(idempotencyKeyHeader);

        var transacaoExistente = transacaoPixRepository.findByIdempotencyKey(idempotencyKey.toString());
        if (transacaoExistente.isPresent()) {
            return transacaoExistente.get();
        }

        UUID correlationId = UUID.randomUUID();
        UUID accountId = usuario.getId();

        TransacaoPix transacao = new TransacaoPix();
        transacao.setChavePixDestino(chavePixDestino);
        transacao.setValor(valor);
        transacao.setDescricao(descricao);
        transacao.setUsuario(usuario);
        transacao.setStatus(StatusTransacao.PENDENTE);
        transacao.setCorrelationId(correlationId);
        transacao.setIdempotencyKey(idempotencyKey);

        TransacaoPix salva = transacaoPixRepository.save(transacao);

        eventoOutboxService.registrarSaqueIniciado(
            salva.getId(),
            accountId,
            idempotencyKey,
            valor,
            correlationId
        );
        return salva;
    }

    public TransacaoPix buscarPorId(UUID id) {
        return transacaoPixRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));
    }

    public List<TransacaoPix> listarPorUsuario(UUID usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }
        return transacaoPixRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    public List<TransacaoPix> listarTodas() {
        return transacaoPixRepository.findAll();
    }

    private UUID parseOrGenerateIdempotencyKey(String header) {
        if (header == null || header.isBlank()) {
            return UUID.randomUUID();
        }

        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Idempotency-Key inválida. Use um UUID válido.");
        }
    }
}