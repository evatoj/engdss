package com.engss.transaction.application.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PixAdapter {

    PixTransferResult transferir(UUID correlationId, String chavePixDestino, BigDecimal valor, String descricao);
}
