package com.engss.transaction.application.service;

import com.engss.transaction.domain.model.StatusTransacao;
import com.engss.transaction.domain.model.TransacaoPix;
import com.engss.transaction.domain.repository.TransacaoPixRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PixSagaService {

    private static final Logger log = LoggerFactory.getLogger(PixSagaService.class);

    private final TransacaoPixRepository transacaoPixRepository;
    private final PixAdapter pixAdapter;
    private final EventoOutboxService eventoOutboxService;

    public PixSagaService(TransacaoPixRepository transacaoPixRepository,
                          PixAdapter pixAdapter,
                          EventoOutboxService eventoOutboxService) {
        this.transacaoPixRepository = transacaoPixRepository;
        this.pixAdapter = pixAdapter;
        this.eventoOutboxService = eventoOutboxService;
    }

    @Transactional
    public void processarSaque(UUID correlationId) {
        TransacaoPix transacao = transacaoPixRepository.findByCorrelationId(correlationId.toString())
                .orElseThrow(() -> new IllegalStateException("Transação não encontrada para correlationId=" + correlationId));

        if (transacao.getStatus() == StatusTransacao.CONCLUIDA || transacao.getStatus() == StatusTransacao.FALHA) {
            log.info("Transação já finalizada. correlationId={}, status={}", correlationId, transacao.getStatus());
            return;
        }

        transacao.setStatus(StatusTransacao.EM_PROCESSAMENTO);

        try {
            PixTransferResult resultado = pixAdapter.transferir(
                    correlationId,
                    transacao.getChavePixDestino(),
                    transacao.getValor(),
                    transacao.getDescricao()
            );

            transacao.setAsaasTransferId(resultado.transferId());
            transacao.setAsaasStatus(resultado.status());
            transacao.setMotivoFalha(resultado.failReason());
            transacaoPixRepository.save(transacao);

            if (resultado.concluidoComSucesso()) {
                eventoOutboxService.registrarPixConfirmado(
                        transacao.getId(),
                        transacao.getUsuario().getId(),
                        transacao.getValor(),
                        correlationId,
                        resultado.transferId()
                );
                return;
            }

            if (resultado.falhou()) {
                transacao.setStatus(StatusTransacao.FALHA);
                transacaoPixRepository.save(transacao);
                eventoOutboxService.registrarPixFalhou(
                        transacao.getId(),
                        transacao.getUsuario().getId(),
                        transacao.getValor(),
                        correlationId,
                        resultado.transferId(),
                        resultado.failReason()
                );
            }

        } catch (Exception e) {
            transacao.setMotivoFalha(e.getMessage());
            transacaoPixRepository.save(transacao);
            eventoOutboxService.registrarPixFalhou(
                    transacao.getId(),
                    transacao.getUsuario().getId(),
                    transacao.getValor(),
                    correlationId,
                    transacao.getAsaasTransferId(),
                    e.getMessage()
            );
        }
    }

    @Transactional
    public void atualizarPorWebhook(String event, String transferId, String externalReference, String status, String failReason) {
        if (externalReference == null || externalReference.isBlank()) {
            return;
        }

        TransacaoPix transacao = transacaoPixRepository.findByCorrelationId(externalReference)
                .orElseThrow(() -> new IllegalStateException("Transação não encontrada para externalReference=" + externalReference));

        transacao.setAsaasTransferId(transferId);
        transacao.setAsaasStatus(status);
        transacao.setMotivoFalha(failReason);

        if ("TRANSFER_DONE".equalsIgnoreCase(event)) {
            if (transacao.getStatus() != StatusTransacao.CONCLUIDA) {
                eventoOutboxService.registrarPixConfirmado(
                        transacao.getId(),
                        transacao.getUsuario().getId(),
                        transacao.getValor(),
                        transacao.getCorrelationId(),
                        transferId
                );
            }
            return;
        }

        if ("TRANSFER_FAILED".equalsIgnoreCase(event) || "TRANSFER_CANCELLED".equalsIgnoreCase(event)) {
            transacao.setStatus(StatusTransacao.FALHA);
            transacaoPixRepository.save(transacao);
            eventoOutboxService.registrarPixFalhou(
                    transacao.getId(),
                    transacao.getUsuario().getId(),
                    transacao.getValor(),
                    transacao.getCorrelationId(),
                    transferId,
                    failReason
            );
        }
    }
}
