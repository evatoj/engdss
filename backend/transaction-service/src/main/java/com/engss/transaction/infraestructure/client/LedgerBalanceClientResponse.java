package com.engss.transaction.infraestructure.client;

import java.math.BigDecimal;

public class LedgerBalanceClientResponse {

    private String accountId;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private String updatedAt;

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getPendingBalance() {
        return pendingBalance;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public void setPendingBalance(BigDecimal pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}