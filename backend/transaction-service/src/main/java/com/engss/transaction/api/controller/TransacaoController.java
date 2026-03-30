package com.engss.transaction.api.controller;

import com.engss.transaction.api.dto.CriarTransacaoRequest;
import com.engss.transaction.api.dto.TransacaoResponse;
import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.application.service.TransacaoPixService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoPixService transacaoPixService;

    public TransacaoController(TransacaoPixService transacaoPixService) {
        this.transacaoPixService = transacaoPixService;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> criarTransacao(@Valid @RequestBody CriarTransacaoRequest request) {
        TransacaoPix transacao = transacaoPixService.criarTransacao(
                request.getUsuarioId(),
                request.getChavePixDestino(),
                request.getValor(),
                request.getDescricao()
        );

        TransacaoResponse response = new TransacaoResponse(
                transacao.getId(),
                transacao.getUsuario().getId(),
                transacao.getChavePixDestino(),
                transacao.getValor(),
                transacao.getDescricao(),
                transacao.getStatus(),
                transacao.getDataCriacao()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponse> buscarPorId(@PathVariable Long id) {
        TransacaoPix transacao = transacaoPixService.buscarPorId(id);

        TransacaoResponse response = new TransacaoResponse(
                transacao.getId(),
                transacao.getUsuario().getId(),
                transacao.getChavePixDestino(),
                transacao.getValor(),
                transacao.getDescricao(),
                transacao.getStatus(),
                transacao.getDataCriacao()
        );

        return ResponseEntity.ok(response);
    }
}