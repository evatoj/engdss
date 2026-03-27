package com.engss.transationService.controller;

import com.engss.transationService.dto.CriarUsuarioRequest;
import com.engss.transationService.dto.SaldoResponse;
import com.engss.transationService.dto.TransacaoResponse;
import com.engss.transationService.dto.UsuarioResponse;
import com.engss.transationService.model.TransacaoPix;
import com.engss.transationService.model.Usuario;
import com.engss.transationService.service.TransacaoPixService;
import com.engss.transationService.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TransacaoPixService transacaoPixService;

    public UsuarioController(UsuarioService usuarioService,
                             TransacaoPixService transacaoPixService) {
        this.usuarioService = usuarioService;
        this.transacaoPixService = transacaoPixService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criarUsuario(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioService.criarUsuario(request.getNome(), request.getSaldoInicial());

        UsuarioResponse response = new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSaldoDisponivel()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> response = usuarioService.listarTodos()
                .stream()
                .map(usuario -> new UsuarioResponse(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getSaldoDisponivel()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);

        SaldoResponse response = new SaldoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSaldoDisponivel()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transacoes")
    public ResponseEntity<List<TransacaoResponse>> listarTransacoesDoUsuario(@PathVariable Long id) {
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
                transacao.getDataCriacao()
        );
    }
}