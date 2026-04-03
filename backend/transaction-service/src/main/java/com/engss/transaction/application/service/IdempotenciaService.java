package com.engss.transaction.application.service;

import com.engss.transaction.application.exception.IdempotenciaEmProcessamentoException;
import com.engss.transaction.domain.model.Idempotencia;
import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.domain.repository.IdempotenciaRepository;
import com.engss.transaction.domain.repository.TransacaoPixRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotenciaService {

    private final IdempotenciaRepository idempotenciaRepository;
    private final TransacaoPixRepository transacaoPixRepository;

    public IdempotenciaService(IdempotenciaRepository idempotenciaRepository,
                               TransacaoPixRepository transacaoPixRepository) {
        this.idempotenciaRepository = idempotenciaRepository;
        this.transacaoPixRepository = transacaoPixRepository;
    }

    public ResultadoIdempotencia verificarOuIniciar(String chave, String payloadCanonico) {
        String requisicaoHash = gerarHash(payloadCanonico);

        try {
            Idempotencia novo = Idempotencia.iniciar(chave, requisicaoHash);
            idempotenciaRepository.saveAndFlush(novo);
            return ResultadoIdempotencia.novoProcessamento();
        } catch (DataIntegrityViolationException e) {
            Idempotencia idem = idempotenciaRepository.findByChave(chave)
                    .orElseThrow(() -> new IdempotenciaEmProcessamentoException("Requisição idempotente concorrente detectada."));

            if (idem.getRequisicaoHash() != null && !idem.getRequisicaoHash().equals(requisicaoHash)) {
                throw new IllegalArgumentException("Idempotency-Key já utilizada com payload diferente.");
            }

            if ("CONCLUIDA".equals(idem.getStatus()) && idem.getTransacaoPixId() != null) {
                TransacaoPix transacaoExistente = transacaoPixRepository.findById(idem.getTransacaoPixId())
                        .orElseThrow(() -> new IllegalStateException("Registro de idempotência aponta para transação inexistente."));
                return ResultadoIdempotencia.replay(transacaoExistente);
            }

            if ("PROCESSANDO".equals(idem.getStatus())) {
                throw new IdempotenciaEmProcessamentoException("Requisição com esta Idempotency-Key ainda está em processamento.");
            }

            throw new IllegalStateException("Status de idempotência inválido para a chave informada.");
        }
    }

    public void concluir(String chave, UUID transacaoPixId) {
        Idempotencia idem = idempotenciaRepository.findByChave(chave)
                .orElseThrow(() -> new IllegalStateException("Registro de idempotência não encontrado para a chave."));

        idem.concluir(transacaoPixId);
        idempotenciaRepository.save(idem);
    }

    private String gerarHash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash de idempotência.", e);
        }
    }
}