package com.engss.ledger.service;

import com.engss.ledger.exception.RecursoNaoEncontradoException;
import com.engss.ledger.model.Usuario;
import com.engss.ledger.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve criar usuário com saldo inicial")
    void deveCriarUsuarioComSaldoInicial() {
        Usuario usuario = usuarioService.criarUsuario("João", new BigDecimal("1000.00"));

        assertNotNull(usuario.getId());
        assertEquals("João", usuario.getNome());
        assertEquals(new BigDecimal("1000.00"), usuario.getSaldo());
    }

    @Test
    @DisplayName("Deve consultar saldo do usuário")
    void deveConsultarSaldoDoUsuario() {
        Usuario usuario = usuarioRepository.save(new Usuario("Maria", new BigDecimal("500.00")));

        BigDecimal saldo = usuarioService.consultarSaldo(usuario.getId());

        assertEquals(new BigDecimal("500.00"), saldo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
    void deveLancarExcecaoAoBuscarUsuarioInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> {
            usuarioService.buscarPorId(999L);
        });
    }

    @Test
    @DisplayName("Não deve criar usuário com nome vazio")
    void naoDeveCriarUsuarioComNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.criarUsuario("", new BigDecimal("100.00"));
        });
    }

    @Test
    @DisplayName("Não deve criar usuário com saldo inicial negativo")
    void naoDeveCriarUsuarioComSaldoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.criarUsuario("Carlos", new BigDecimal("-10.00"));
        });
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosOsUsuarios() {
        usuarioRepository.save(new Usuario("João", new BigDecimal("100.00")));
        usuarioRepository.save(new Usuario("Maria", new BigDecimal("200.00")));

        var usuarios = usuarioService.listarTodos();

        assertEquals(2, usuarios.size());
    }
}