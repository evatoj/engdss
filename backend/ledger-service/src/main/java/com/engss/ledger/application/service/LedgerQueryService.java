package com.engss.ledger.application.service;

import com.engss.ledger.api.dto.BalanceResponse;
import com.engss.ledger.api.dto.StatementResponse;
import com.engss.ledger.application.exception.AccountNotFoundException;
import com.engss.ledger.domain.repository.BalanceViewRepository;
import com.engss.ledger.domain.repository.LedgerEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerQueryService {

    private final BalanceViewRepository balanceViewRepository;
    private final LedgerEventRepository ledgerEventRepository;

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID accountId) {
        var view = balanceViewRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        return new BalanceResponse(
            view.getAccountId(),
            view.getAvailableBalance(),
            view.getPendingBalance(),
            view.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<StatementResponse> getStatement(UUID accountId) {
        return ledgerEventRepository
            .findByAccountIdOrderByOccurredAtAsc(accountId)
            .stream()
            .map(e -> new StatementResponse(
                e.getId(), e.getType(), e.getAmount(),
                e.getOccurredAt(), e.getCorrelationId()
            ))
            .toList();
    }
}