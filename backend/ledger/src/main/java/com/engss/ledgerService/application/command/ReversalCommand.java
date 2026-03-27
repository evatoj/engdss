package com.engss.ledgerService.application.command;
import java.util.UUID;
public record ReversalCommand(UUID accountId, UUID correlationId) {}