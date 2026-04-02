package com.engss.transaction.api.controller;

import com.engss.transaction.api.dto.AsaasTransferWebhookPayload;
import com.engss.transaction.application.service.PixSagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/asaas")
public class AsaasWebhookController {

    private static final Logger log = LoggerFactory.getLogger(AsaasWebhookController.class);

    private final PixSagaService pixSagaService;
    private final String webhookToken;

    public AsaasWebhookController(PixSagaService pixSagaService,
                                  @Value("${asaas.webhook.auth-token:}") String webhookToken) {
        this.pixSagaService = pixSagaService;
        this.webhookToken = webhookToken;
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> receberTransferencias(
            @RequestBody AsaasTransferWebhookPayload payload,
            @RequestHeader(value = "asaas-access-token", required = false) String token) {

        if (webhookToken != null && !webhookToken.isBlank() && !webhookToken.equals(token)) {
            log.warn("Webhook Asaas rejeitado por token inválido. event={}", payload.event());
            return ResponseEntity.status(401).build();
        }

        if (payload.transfer() != null) {
            pixSagaService.atualizarPorWebhook(
                    payload.event(),
                    payload.transfer().id(),
                    payload.transfer().externalReference(),
                    payload.transfer().status(),
                    payload.transfer().failReason()
            );
        }

        return ResponseEntity.ok().build();
    }
}
