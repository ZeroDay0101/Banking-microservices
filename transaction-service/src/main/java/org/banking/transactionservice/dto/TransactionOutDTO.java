package org.banking.transactionservice.dto;

import org.banking.transactionservice.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionOutDTO(
        String id,
        String userId,
        String recipient,
        BigDecimal amount,
        LocalDateTime date,
        TransactionType type
) {
}
