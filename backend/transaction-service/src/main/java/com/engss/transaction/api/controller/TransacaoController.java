package com.engss.transaction.api.controller;

import com.engss.transaction.api.dto.CriarTransacaoRequest;
import com.engss.transaction.api.dto.TransacaoResponse;
import com.engss.transaction.application.service.ResultadoCriacaoTransacao;
import com.engss.transaction.application.service.TransacaoPixService;
import com.engss.transaction.domain.model.TransacaoPix;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoPixService transacaoPixService;

    public TransacaoController(TransacaoPixService transacaoPixService) {
        this.transacaoPixService = transacaoPixService;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> criarTransacao(
            @Valid @RequestBody CriarTransacaoRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        ResultadoCriacaoTransacao resultado = transacaoPixService.criarTransacao(
                request.getUsuarioId(),
                request.getChavePixDestino(),
                request.getValor(),
                request.getDescricao(),
                idempotencyKey
        );

        HttpStatus status = resultado.replayIdempotente() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(toResponse(resultado.transacao()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponse> buscarPorId(@PathVariable UUID id) {
        TransacaoPix transacao = transacaoPixService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(transacao));
    }

    private TransacaoResponse toResponse(TransacaoPix transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getUsuario().getId(),
                transacao.getChavePixDestino(),
                transacao.getValor(),
                transacao.getDescricao(),
                transacao.getStatus(),
                transacao.getDataCriacao()
                // transacao.getAsaasTransferId(),
                // transacao.getAsaasStatus(),
                // transacao.getMotivoFalha()
        );
    }
}
