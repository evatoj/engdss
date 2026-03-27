package com.engss.transationService.service;

import com.engss.transationService.exception.RecursoNaoEncontradoException;
import com.engss.transationService.model.StatusTransacao;
import com.engss.transationService.model.TransacaoPix;
import com.engss.transationService.model.Usuario;
//import com.engss.transationService.repository.TransacaoPixRepository;
import com.engss.transationService.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransacaoPixServiceTest {

    @Autowired
    private TransacaoPixService transacaoPixService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    //@Autowired
    //private TransacaoPixRepository transacaoPixRepository;

    @Test
    @DisplayName("Deve criar transação PIX e debitar saldo do usuário")
    void deveCriarTransacaoPixEDebitarSaldo() {
        Usuario usuario = usuarioRepository.save(new Usuario("Ana", new BigDecimal("1000.00")));

        TransacaoPix transacao = transacaoPixService.criarTransacao(
                usuario.getId(),
                "ana@email.com",
                new BigDecimal("200.00"),
                "Pagamento teste"
        );

        Usuario usuarioAtualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();

        assertNotNull(transacao.getId());
        assertEquals(StatusTransacao.CONCLUIDA, transacao.getStatus());
        assertEquals(new BigDecimal("800.00"), usuarioAtualizado.getSaldoDisponivel());
    }

    @Test
    @DisplayName("Não deve criar transação se saldo for insuficiente")
    void naoDeveCriarTransacaoComSaldoInsuficiente() {
        Usuario usuario = usuarioRepository.save(new Usuario("Pedro", new BigDecimal("50.00")));

        assertThrows(IllegalArgumentException.class, () -> {
            transacaoPixService.criarTransacao(
                    usuario.getId(),
                    "pix@teste.com",
                    new BigDecimal("100.00"),
                    "Pagamento maior que saldo"
            );
        });
    }

    @Test
    @DisplayName("Deve buscar transação por id")
    void deveBuscarTransacaoPorId() {
        Usuario usuario = usuarioRepository.save(new Usuario("Julia", new BigDecimal("500.00")));

        TransacaoPix criada = transacaoPixService.criarTransacao(
                usuario.getId(),
                "julia@email.com",
                new BigDecimal("100.00"),
                "Compra"
        );

        TransacaoPix encontrada = transacaoPixService.buscarPorId(criada.getId());

        assertEquals(criada.getId(), encontrada.getId());
        assertEquals(new BigDecimal("100.00"), encontrada.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação inexistente")
    void deveLancarExcecaoAoBuscarTransacaoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> {
            transacaoPixService.buscarPorId(999L);
        });
    }

    @Test
    @DisplayName("Deve listar histórico de transações do usuário")
    void deveListarHistoricoDeTransacoesDoUsuario() {
        Usuario usuario = usuarioRepository.save(new Usuario("Bruno", new BigDecimal("1000.00")));

        transacaoPixService.criarTransacao(usuario.getId(), "a@a.com", new BigDecimal("100.00"), "T1");
        transacaoPixService.criarTransacao(usuario.getId(), "b@b.com", new BigDecimal("150.00"), "T2");

        List<TransacaoPix> transacoes = transacaoPixService.listarPorUsuario(usuario.getId());

        assertEquals(2, transacoes.size());
    }

    @Test
    @DisplayName("Não deve criar transação para usuário inexistente")
    void naoDeveCriarTransacaoParaUsuarioInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> {
            transacaoPixService.criarTransacao(
                    999L,
                    "teste@email.com",
                    new BigDecimal("100.00"),
                    "Pagamento");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar transações de usuário inexistente")
    void deveLancarExcecaoAoListarTransacoesDeUsuarioInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> {
            transacaoPixService.listarPorUsuario(999L);
        });
    }
}