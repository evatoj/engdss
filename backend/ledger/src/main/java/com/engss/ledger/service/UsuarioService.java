package com.engss.ledger.service;

import com.engss.ledger.exception.RecursoNaoEncontradoException;
import com.engss.ledger.model.Usuario;
import com.engss.ledger.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criarUsuario(String nome, BigDecimal saldoInicial) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }

        if (saldoInicial == null || saldoInicial.signum() < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }

        Usuario usuario = new Usuario(nome, saldoInicial);
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    public BigDecimal consultarSaldo(Long usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        return usuario.getSaldoDisponivel();
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}