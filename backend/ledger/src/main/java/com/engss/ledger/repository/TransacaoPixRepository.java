package com.engss.ledger.repository;

import com.engss.ledger.model.TransacaoPix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoPixRepository extends JpaRepository<TransacaoPix, Long> {
    List<TransacaoPix> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);
}