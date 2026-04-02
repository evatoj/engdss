package com.engss.transaction.application.service;

public record PixTransferResult(
        String transferId,
        String status,
        String failReason
) {

    public boolean concluidoComSucesso() {
        return "DONE".equalsIgnoreCase(status);
    }

    public boolean falhou() {
        return "FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status);
    }
}
