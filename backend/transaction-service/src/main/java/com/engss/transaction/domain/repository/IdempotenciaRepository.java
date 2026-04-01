package com.engss.transaction.domain.repository;

import com.engss.transaction.domain.model.Idempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotenciaRepository extends JpaRepository<Idempotencia, Long> {

    Optional<Idempotencia> findByChave(String chave);
}