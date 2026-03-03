package org.banking.commonlib.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountTransferEvent(
        String accountId,
        String recipientId,
        BigDecimal amount,
        LocalDateTime occurredAt
) {
}
