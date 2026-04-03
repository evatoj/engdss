package com.engss.transaction.domain.repository;

import com.engss.transaction.domain.model.EventoOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, Long> {

    List<EventoOutbox> findTop20ByStatusOrderByDataCriacaoAsc(String status);
}