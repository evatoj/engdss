package com.engss.ledgerService.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "balance_view")
public class BalanceView {

    @Id
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pendingBalance;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected BalanceView() {}

    public BalanceView(UUID accountId, BigDecimal availableBalance,
                       BigDecimal pendingBalance, Instant updatedAt, Long version) {
        this.accountId = accountId;
        this.availableBalance = availableBalance;
        this.pendingBalance = pendingBalance;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public UUID getAccountId() { return accountId; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public BigDecimal getPendingBalance() { return pendingBalance; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void apply(LedgerEvent event) {
        switch (event.getType()) {
            case DEBIT_PENDING -> {
                this.availableBalance = this.availableBalance.subtract(event.getAmount());
                this.pendingBalance = this.pendingBalance.add(event.getAmount());
            }
            case DEBIT_CONFIRMED -> {
                this.pendingBalance = this.pendingBalance.subtract(event.getAmount());
            }
            case CREDIT -> {
                this.availableBalance = this.availableBalance.add(event.getAmount());
            }
            case REVERSAL -> {
                this.pendingBalance = this.pendingBalance.subtract(event.getAmount());
                this.availableBalance = this.availableBalance.add(event.getAmount());
            }
        }
        this.updatedAt = Instant.now();
    }
}