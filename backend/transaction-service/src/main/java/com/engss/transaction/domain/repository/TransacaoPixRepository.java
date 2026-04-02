package com.engss.transaction.domain.repository;

import com.engss.transaction.domain.model.TransacaoPix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoPixRepository extends JpaRepository<TransacaoPix, UUID> {

    List<TransacaoPix> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    @Query("SELECT t FROM TransacaoPix t WHERE t.correlationId = :correlationId")
    Optional<TransacaoPix> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT t FROM TransacaoPix t WHERE t.idempotencyKey = :idempotencyKey")
    Optional<TransacaoPix> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    Optional<TransacaoPix> findByAsaasTransferId(String asaasTransferId);
}
