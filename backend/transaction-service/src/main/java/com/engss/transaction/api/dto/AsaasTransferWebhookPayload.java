package com.engss.transaction.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasTransferWebhookPayload(
        String id,
        String event,
        TransferWebhook transfer
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransferWebhook(
            String id,
            String status,
            String externalReference,
            String failReason
    ) {}
}
