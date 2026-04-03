package com.engss.transaction.application.service;

import com.engss.transaction.application.exception.RecursoNaoEncontradoException;
import com.engss.transaction.domain.model.Usuario;
import com.engss.transaction.domain.repository.UsuarioRepository;
//import com.engss.transaction.infraestructure.messaging.EventoOutboxService;
import com.engss.transaction.application.service.EventoOutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EventoOutboxService eventoOutboxService;
    
    public UsuarioService(UsuarioRepository usuarioRepository,
                          EventoOutboxService pixEventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.eventoOutboxService = pixEventPublisher;
    }

    @Transactional
    public Usuario criarUsuario(String nome, BigDecimal saldoInicial) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        if (saldoInicial == null || saldoInicial.signum() < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }

        Usuario usuario = new Usuario(nome);
        Usuario salvo = usuarioRepository.save(usuario);

        if (saldoInicial.signum() > 0) {
            UUID accountId = salvo.getId();
            UUID idempotencyKey = UUID.randomUUID();
            UUID correlationId = UUID.randomUUID();

            eventoOutboxService.registrarCreditoInicial(
                    accountId,
                    idempotencyKey,
                    saldoInicial,
                    correlationId
            );
        }

        return salvo;
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}