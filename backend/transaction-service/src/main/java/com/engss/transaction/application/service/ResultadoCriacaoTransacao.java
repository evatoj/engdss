package com.engss.transaction.application.service;

import com.engss.transaction.domain.model.TransacaoPix;

public record ResultadoCriacaoTransacao(TransacaoPix transacao, boolean replayIdempotente) {
}
