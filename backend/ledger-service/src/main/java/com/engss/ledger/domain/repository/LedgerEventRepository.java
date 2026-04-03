package com.engss.ledger.domain.repository;

import com.engss.ledger.domain.model.LedgerEvent;
import com.engss.ledger.domain.model.LedgerEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEventRepository extends JpaRepository<LedgerEvent, UUID> {

    List<LedgerEvent> findByAccountIdOrderByOccurredAtAsc(UUID accountId);

    Optional<LedgerEvent> findByCorrelationIdAndType(UUID correlationId, LedgerEventType type);

    Optional<LedgerEvent> findByIdempotencyKeyAndType(UUID idempotencyKey, LedgerEventType type);

    boolean existsByIdempotencyKey(UUID idempotencyKey);
}