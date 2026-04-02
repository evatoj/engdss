package com.engss.transaction.infraestructure.asaas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasErrorResponse {

    private List<AsaasErrorItem> errors;

    public List<AsaasErrorItem> getErrors() {
        return errors;
    }

    public void setErrors(List<AsaasErrorItem> errors) {
        this.errors = errors;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AsaasErrorItem {
        private String code;
        private String description;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}