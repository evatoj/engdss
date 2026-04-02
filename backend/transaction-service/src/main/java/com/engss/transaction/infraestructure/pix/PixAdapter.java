package com.engss.transaction.infraestructure.pix;

import com.engss.transaction.domain.model.TransacaoPix;

public interface PixAdapter {

    PixTransferResult transferir(TransacaoPix transacao);
}