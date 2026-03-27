package com.engss.ledgerService.api.controller;

import com.engss.ledgerService.application.service.LedgerQueryService;
import com.engss.ledgerService.api.dto.BalanceResponse;
import com.engss.ledgerService.api.dto.StatementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ledger/queries")
@RequiredArgsConstructor
public class LedgerQueryController {

    private final LedgerQueryService ledgerQueryService;

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ledgerQueryService.getBalance(accountId));
    }

    @GetMapping("/statement/{accountId}")
    public ResponseEntity<List<StatementResponse>> getStatement(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ledgerQueryService.getStatement(accountId));
    }
}