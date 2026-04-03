package com.engss.ledger.domain.repository;

import com.engss.ledger.domain.model.BalanceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface BalanceViewRepository extends JpaRepository<BalanceView, UUID> {

    // lock pessimista — evita race condition ao atualizar saldo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BalanceView b WHERE b.accountId = :accountId")
    Optional<BalanceView> findByIdForUpdate(@Param("accountId") UUID accountId);
}