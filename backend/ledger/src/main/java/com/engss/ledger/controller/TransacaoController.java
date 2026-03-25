package com.engss.ledger.controller;

import com.engss.ledger.dto.CriarTransacaoRequest;
import com.engss.ledger.dto.TransacaoResponse;
import com.engss.ledger.model.TransacaoPix;
import com.engss.ledger.service.TransacaoPixService;
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