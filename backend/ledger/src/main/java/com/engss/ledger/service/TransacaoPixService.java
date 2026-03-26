package com.engss.ledger.service;

import com.engss.ledger.exception.RecursoNaoEncontradoException;
import com.engss.ledger.model.StatusTransacao;
import com.engss.ledger.model.TransacaoPix;
import com.engss.ledger.model.Usuario;
import com.engss.ledger.repository.TransacaoPixRepository;
import com.engss.ledger.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoPixService {

    private final TransacaoPixRepository transacaoPixRepository;
    private final UsuarioRepository usuarioRepository;

    public TransacaoPixService(TransacaoPixRepository transacaoPixRepository,
                               UsuarioRepository usuarioRepository) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.usuarioRepository = usuarioRepository;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));;

        usuario.reservarSaldo(valor);

        TransacaoPix transacao = new TransacaoPix();
        transacao.setChavePixDestino(chavePixDestino);
        transacao.setValor(valor);
        transacao.setDescricao(descricao);
        transacao.setUsuario(usuario);
        transacao.setStatus(StatusTransacao.CONCLUIDA);

        usuarioRepository.save(usuario);
        return transacaoPixRepository.save(transacao);
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