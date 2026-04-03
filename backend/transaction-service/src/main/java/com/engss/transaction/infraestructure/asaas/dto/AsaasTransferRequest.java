package com.engss.transaction.infraestructure.asaas.dto;

import java.math.BigDecimal;

public class AsaasTransferRequest {

    private BigDecimal value;
    private String operationType;
    private String pixAddressKey;
    private String description;
    private String externalReference;

    public AsaasTransferRequest(
            BigDecimal value,
            String operationType,
            String pixAddressKey,
            String description,
            String externalReference
    ) {
        this.value = value;
        this.operationType = operationType;
        this.pixAddressKey = pixAddressKey;
        this.description = description;
        this.externalReference = externalReference;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getPixAddressKey() {
        return pixAddressKey;
    }

    public String getDescription() {
        return description;
    }

    public String getExternalReference() {
        return externalReference;
    }
}