package com.engss.ledger.controller;

import com.engss.ledger.model.StatusTransacao;
import com.engss.ledger.model.TransacaoPix;
import com.engss.ledger.model.Usuario;
import com.engss.ledger.service.TransacaoPixService;
import com.engss.ledger.service.UsuarioService;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TransacaoPixService transacaoPixService;

    @Test
    @DisplayName("POST /usuarios deve criar usuário")
    void postUsuariosDeveCriarUsuario() throws Exception {
        Usuario usuario = new Usuario("João", new BigDecimal("1000.00"));
        usuario.setId(1L);

        Mockito.when(usuarioService.criarUsuario(eq("João"), eq(new BigDecimal("1000.00"))))
                .thenReturn(usuario);

        String body = """
                {
                  "nome": "João",
                  "saldoInicial": 1000.00
                }
                """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.saldo").value(1000.00));
    }

    @Test
    @DisplayName("GET /usuarios deve listar usuários")
    void getUsuariosDeveListarUsuarios() throws Exception {
        Usuario u1 = new Usuario("João", new BigDecimal("1000.00"));
        u1.setId(1L);

        Usuario u2 = new Usuario("Maria", new BigDecimal("500.00"));
        u2.setId(2L);

        Mockito.when(usuarioService.listarTodos()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("João"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nome").value("Maria"));
    }

    @Test
    @DisplayName("GET /usuarios/{id}/saldo deve retornar saldo")
    void getSaldoDeveRetornarSaldo() throws Exception {
        Usuario usuario = new Usuario("João", new BigDecimal("700.00"));
        usuario.setId(1L);

        Mockito.when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/1/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.saldo").value(700.00));
    }

    @Test
    @DisplayName("POST /usuarios com body inválido deve retornar 400")
    void postUsuariosComBodyInvalidoDeveRetornar400() throws Exception {
        String body = """
                {
                  "nome": "",
                  "saldoInicial": -10
                }
                """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /usuarios/{id}/transacoes deve retornar histórico de transações")
    void getTransacoesDoUsuarioDeveRetornarHistorico() throws Exception {
            Usuario usuario = new Usuario("João", new BigDecimal("700.00"));
            usuario.setId(1L);

            TransacaoPix t1 = new TransacaoPix();
            t1.setId(10L);
            t1.setUsuario(usuario);
            t1.setChavePixDestino("pix1@email.com");
            t1.setValor(new BigDecimal("100.00"));
            t1.setDescricao("Pagamento 1");
            t1.setStatus(StatusTransacao.CONCLUIDA);
            t1.setDataCriacao(LocalDateTime.now());

            TransacaoPix t2 = new TransacaoPix();
            t2.setId(11L);
            t2.setUsuario(usuario);
            t2.setChavePixDestino("pix2@email.com");
            t2.setValor(new BigDecimal("50.00"));
            t2.setDescricao("Pagamento 2");
            t2.setStatus(StatusTransacao.CONCLUIDA);
            t2.setDataCriacao(LocalDateTime.now());

            Mockito.when(transacaoPixService.listarPorUsuario(1L)).thenReturn(List.of(t1, t2));

            mockMvc.perform(get("/usuarios/1/transacoes"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[0].id").value(10))
                            .andExpect(jsonPath("$[0].usuarioId").value(1))
                            .andExpect(jsonPath("$[0].chavePixDestino").value("pix1@email.com"))
                            .andExpect(jsonPath("$[0].valor").value(100.00))
                            .andExpect(jsonPath("$[1].id").value(11))
                            .andExpect(jsonPath("$[1].usuarioId").value(1))
                            .andExpect(jsonPath("$[1].chavePixDestino").value("pix2@email.com"))
                            .andExpect(jsonPath("$[1].valor").value(50.00));
    }

    @Test
    @DisplayName("GET /usuarios/{id}/saldo deve retornar 404 quando usuário não existir")
    void getSaldoDeveRetornar404QuandoUsuarioNaoExistir() throws Exception {
            Mockito.when(usuarioService.buscarPorId(999L))
                            .thenThrow(new com.engss.ledger.exception.RecursoNaoEncontradoException(
                                            "Usuário não encontrado."));

            mockMvc.perform(get("/usuarios/999/saldo"))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404))
                            .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                            .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado."))
                            .andExpect(jsonPath("$.caminho").value("/usuarios/999/saldo"));
    }
}