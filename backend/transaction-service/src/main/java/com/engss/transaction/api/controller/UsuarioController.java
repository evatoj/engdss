package com.engss.transaction.api.controller;

import com.engss.transaction.api.dto.CriarUsuarioRequest;
import com.engss.transaction.api.dto.SaldoResponse;
import com.engss.transaction.api.dto.TransacaoResponse;
import com.engss.transaction.api.dto.UsuarioResponse;
import com.engss.transaction.application.service.TransacaoPixService;
import com.engss.transaction.application.service.UsuarioService;
import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.domain.model.Usuario;
import com.engss.transaction.infraestructure.client.LedgerBalanceClientResponse;
import com.engss.transaction.infraestructure.client.LedgerQueryClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TransacaoPixService transacaoPixService;
    private final LedgerQueryClient ledgerQueryClient;

    public UsuarioController(UsuarioService usuarioService,
                             TransacaoPixService transacaoPixService,
                             LedgerQueryClient ledgerQueryClient) {
        this.usuarioService = usuarioService;
        this.transacaoPixService = transacaoPixService;
        this.ledgerQueryClient = ledgerQueryClient;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criarUsuario(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioService.criarUsuario(request.getNome(), request.getSaldoInicial());

        // removo a consulta do ledger para evitar o race condition
        // //BigDecimal saldo = ledgerQueryClient.getAvailableBalanceOrZero(usuario.getId());
        BigDecimal saldo = request.getSaldoInicial();

        UsuarioResponse response = new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                saldo
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> response = usuarioService.listarTodos()
                .stream()
                .map(usuario -> {
                    BigDecimal saldo = ledgerQueryClient.getAvailableBalanceOrZero(usuario.getId());

                    return new UsuarioResponse(
                            usuario.getId(),
                            usuario.getNome(),
                            saldo
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable UUID id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        LedgerBalanceClientResponse balance = ledgerQueryClient.getBalanceOrThrow(usuario.getId());

        SaldoResponse response = new SaldoResponse(
                usuario.getId(),
                usuario.getNome(),
                balance.getAvailableBalance()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transacoes")
    public ResponseEntity<List<TransacaoResponse>> listarTransacoesDoUsuario(@PathVariable UUID id) {
        List<TransacaoPix> transacoes = transacaoPixService.listarPorUsuario(id);

        List<TransacaoResponse> response = transacoes.stream()
                .map(this::toTransacaoResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private TransacaoResponse toTransacaoResponse(TransacaoPix transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getUsuario().getId(),
                transacao.getChavePixDestino(),
                transacao.getValor(),
                transacao.getDescricao(),
                transacao.getStatus(),
                transacao.getDataCriacao(),
                transacao.getAsaasTransferId(),
                transacao.getAsaasStatus(),
                transacao.getMotivoFalha()
        );
    }
}