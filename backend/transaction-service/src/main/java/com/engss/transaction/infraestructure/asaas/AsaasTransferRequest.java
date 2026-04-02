package com.engss.transaction.infraestructure.asaas;

import java.math.BigDecimal;

public record AsaasTransferRequest(
        BigDecimal value,
        String operationType,
        String pixAddressKey,
        String description,
        String externalReference
) {
}
