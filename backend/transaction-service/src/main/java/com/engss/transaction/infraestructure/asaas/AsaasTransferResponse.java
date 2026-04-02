package com.engss.transaction.infraestructure.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasTransferResponse(
        String id,
        String status,
        String failReason,
        String externalReference
) {
}
