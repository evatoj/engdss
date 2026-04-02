package com.engss.transaction.infraestructure.asaas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasTransferResponse {

    private String id;
    private String status;
    private String failReason;

    public AsaasTransferResponse() {
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}