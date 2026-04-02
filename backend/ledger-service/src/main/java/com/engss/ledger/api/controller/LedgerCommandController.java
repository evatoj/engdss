package com.engss.ledger.api.controller;

import com.engss.ledger.application.command.DebitCommand;
import com.engss.ledger.application.command.ReversalCommand;
import com.engss.ledger.application.service.LedgerCommandService;
import com.engss.ledger.api.dto.DebitRequest;
import com.engss.ledger.api.dto.CreditRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ledger/commands")
@RequiredArgsConstructor
public class LedgerCommandController {

    private final LedgerCommandService ledgerCommandService;

    @PostMapping("/debit")
    public ResponseEntity<?> debit(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @RequestHeader("X-Correlation-Id") UUID correlationId,
            @Valid @RequestBody DebitRequest request) {

        boolean aprovado = ledgerCommandService.debitPending(new DebitCommand(
            request.accountId(), idempotencyKey, request.amount(), correlationId
        ));

        if (!aprovado) {
            return ResponseEntity
                    .status(402)
                    .body(Map.of(
                        "status", "DENIED",
                        "reason", "Saldo insuficiente para realizar a operação.",
                        "accountId", request.accountId(),
                        "correlationId", correlationId
                    ));
        }

        return ResponseEntity.accepted().build(); // 202 — fluxo assíncrono
    }

    @PostMapping("/reversal")
    public ResponseEntity<Void> reverse(
            @RequestHeader("X-Correlation-Id") UUID correlationId,
            @RequestParam UUID accountId) {

        ledgerCommandService.reverse(new ReversalCommand(accountId, correlationId));
        return ResponseEntity.accepted().build();
    }
}
