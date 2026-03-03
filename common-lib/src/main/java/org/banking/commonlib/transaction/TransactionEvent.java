package org.banking.commonlib.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class TransactionEvent {
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;

    public TransactionEvent(BigDecimal amount, LocalDateTime occurredAt) {
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
