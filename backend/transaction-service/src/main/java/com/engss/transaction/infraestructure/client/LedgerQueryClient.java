package com.engss.transaction.infraestructure.client;

import com.engss.transaction.application.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LedgerQueryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ledger.base-url}")
    private String ledgerBaseUrl;

    public LedgerBalanceClientResponse getBalance(UUID accountId) {
        String url = ledgerBaseUrl + "/ledger/queries/balance/" + accountId;
        ResponseEntity<LedgerBalanceClientResponse> response =
                restTemplate.getForEntity(url, LedgerBalanceClientResponse.class);
        return response.getBody();
    }

    public BigDecimal getAvailableBalanceOrZero(UUID accountId) {
        try {
            LedgerBalanceClientResponse response = getBalance(accountId);
            if (response == null || response.getAvailableBalance() == null) {
                return BigDecimal.ZERO;
            }
            return response.getAvailableBalance();
        } catch (HttpClientErrorException.NotFound e) {
            return BigDecimal.ZERO;
        }
    }

    public LedgerBalanceClientResponse getBalanceOrThrow(UUID accountId) {
        try {
            LedgerBalanceClientResponse response = getBalance(accountId);
            if (response == null) {
                throw new RecursoNaoEncontradoException("Saldo não encontrado no ledger.");
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNaoEncontradoException("Conta não encontrada no ledger: " + accountId);
        }
    }
}