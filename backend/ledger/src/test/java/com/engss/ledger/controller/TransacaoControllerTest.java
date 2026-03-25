package com.engss.ledger.controller;

import com.engss.ledger.model.StatusTransacao;
import com.engss.ledger.model.TransacaoPix;
import com.engss.ledger.model.Usuario;
import com.engss.ledger.service.TransacaoPixService;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoPixService transacaoPixService;

    @Test
    @DisplayName("POST /transacoes deve criar transação")
    void postTransacoesDeveCriarTransacao() throws Exception {
        Usuario usuario = new Usuario("João", new BigDecimal("1000.00"));
        usuario.setId(1L);

        TransacaoPix transacao = new TransacaoPix();
        transacao.setId(1L);
        transacao.setUsuario(usuario);
        transacao.setChavePixDestino("teste@email.com");
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setDescricao("Pagamento");
        transacao.setStatus(StatusTransacao.CONCLUIDA);
        transacao.setDataCriacao(LocalDateTime.now());

        Mockito.when(transacaoPixService.criarTransacao(
                        eq(1L),
                        eq("teste@email.com"),
                        eq(new BigDecimal("100.00")),
                        eq("Pagamento")
                ))
                .thenReturn(transacao);

        String body = """
                {
                  "usuarioId": 1,
                  "chavePixDestino": "teste@email.com",
                  "valor": 100.00,
                  "descricao": "Pagamento"
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.chavePixDestino").value("teste@email.com"))
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("GET /transacoes/{id} deve retornar transação")
    void getTransacaoPorIdDeveRetornarTransacao() throws Exception {
        Usuario usuario = new Usuario("Maria", new BigDecimal("900.00"));
        usuario.setId(2L);

        TransacaoPix transacao = new TransacaoPix();
        transacao.setId(10L);
        transacao.setUsuario(usuario);
        transacao.setChavePixDestino("pix@email.com");
        transacao.setValor(new BigDecimal("200.00"));
        transacao.setDescricao("Teste");
        transacao.setStatus(StatusTransacao.CONCLUIDA);
        transacao.setDataCriacao(LocalDateTime.now());

        Mockito.when(transacaoPixService.buscarPorId(10L)).thenReturn(transacao);

        mockMvc.perform(get("/transacoes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.valor").value(200.00))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("POST /transacoes com body inválido deve retornar 400")
    void postTransacoesComBodyInvalidoDeveRetornar400() throws Exception {
        String body = """
                {
                  "usuarioId": null,
                  "chavePixDestino": "",
                  "valor": 0,
                  "descricao": ""
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}