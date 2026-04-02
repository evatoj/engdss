package com.engss.transaction.infraestructure.asaas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasErrorResponse(List<AsaasErrorItem> errors) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AsaasErrorItem(String code, String description) {}
}
