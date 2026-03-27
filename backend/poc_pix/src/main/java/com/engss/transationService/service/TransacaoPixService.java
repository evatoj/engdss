package com.engss.transationService.service;

import com.engss.transationService.exception.RecursoNaoEncontradoException;
import com.engss.transationService.messaging.PixEventPublisher;
import com.engss.transationService.model.StatusTransacao;
import com.engss.transationService.model.TransacaoPix;
import com.engss.transationService.model.Usuario;
import com.engss.transationService.repository.TransacaoPixRepository;
import com.engss.transationService.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransacaoPixService {

    private final TransacaoPixRepository transacaoPixRepository;
    private final UsuarioRepository usuarioRepository;
    private final PixEventPublisher pixEventPublisher;

    public TransacaoPixService(TransacaoPixRepository transacaoPixRepository,
                                UsuarioRepository usuarioRepository,
                                PixEventPublisher pixEventPublisher) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.usuarioRepository = usuarioRepository;
        this.pixEventPublisher = pixEventPublisher;
    }

    @Transactional
    public TransacaoPix criarTransacao(Long usuarioId,
                                       String chavePixDestino,
                                       BigDecimal valor,
                                       String descricao) {
        if (chavePixDestino == null || chavePixDestino.isBlank()) {
            throw new IllegalArgumentException("A chave PIX de destino é obrigatória.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        // reserva o saldo (disponível → pendente)
        usuario.reservarSaldo(valor);
        usuarioRepository.save(usuario);

        // gera IDs para rastreamento
        UUID correlationId  = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        UUID accountId      = UUID.nameUUIDFromBytes(("usuario-" + usuarioId).getBytes());

        // salva transação como PENDENTE
        TransacaoPix transacao = new TransacaoPix();
        transacao.setChavePixDestino(chavePixDestino);
        transacao.setValor(valor);
        transacao.setDescricao(descricao);
        transacao.setUsuario(usuario);
        transacao.setStatus(StatusTransacao.PENDENTE);
        transacao.setCorrelationId(correlationId);
        TransacaoPix salva = transacaoPixRepository.save(transacao);

        // publica evento para o ledger processar
        pixEventPublisher.publishSaqueIniciado(accountId, idempotencyKey, valor, correlationId);

        return salva;
    }

    public TransacaoPix buscarPorId(Long id) {
        return transacaoPixRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada."));
    }

    public List<TransacaoPix> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }
        return transacaoPixRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    public List<TransacaoPix> listarTodas() {
        return transacaoPixRepository.findAll();
    }
}