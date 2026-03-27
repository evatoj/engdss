package com.engss.ledgerService.api.controller;

import com.engss.ledgerService.application.command.DebitCommand;
import com.engss.ledgerService.application.command.ReversalCommand;
import com.engss.ledgerService.application.service.LedgerCommandService;
import com.engss.ledgerService.api.dto.DebitRequest;
import com.engss.ledgerService.api.dto.CreditRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ledger/commands")
@RequiredArgsConstructor
public class LedgerCommandController {

    private final LedgerCommandService ledgerCommandService;

    @PostMapping("/debit")
    public ResponseEntity<Void> debit(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @RequestHeader("X-Correlation-Id") UUID correlationId,
            @Valid @RequestBody DebitRequest request) {

        ledgerCommandService.debitPending(new DebitCommand(
            request.accountId(), idempotencyKey, request.amount(), correlationId
        ));
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