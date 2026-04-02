package com.engss.transaction.infraestructure.pix;

public class PixTransferResult {

    private final boolean sucesso;
    private final String transferId;
    private final String status;
    private final String motivoFalha;

    public PixTransferResult(boolean sucesso, String transferId, String status, String motivoFalha) {
        this.sucesso = sucesso;
        this.transferId = transferId;
        this.status = status;
        this.motivoFalha = motivoFalha;
    }

    public static PixTransferResult sucesso(String transferId, String status) {
        return new PixTransferResult(true, transferId, status, null);
    }

    public static PixTransferResult falha(String transferId, String status, String motivoFalha) {
        return new PixTransferResult(false, transferId, status, motivoFalha);
    }

    public boolean concluidoComSucesso() {
        return sucesso;
    }

    public boolean falhou() {
        return !sucesso;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getStatus() {
        return status;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }
}