package com.engss.transaction.application.service;

import com.engss.transaction.domain.model.TransacaoPix;

public record ResultadoIdempotencia(boolean deveProcessar, boolean replay, TransacaoPix transacaoExistente) {

    public static ResultadoIdempotencia novoProcessamento() {
        return new ResultadoIdempotencia(true, false, null);
    }

    public static ResultadoIdempotencia replay(TransacaoPix transacaoExistente) {
        return new ResultadoIdempotencia(false, true, transacaoExistente);
    }
}
