package org.banking.commonlib.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDepositEvent(
        String accountId,
        BigDecimal amount,
        LocalDateTime occurredAt
) {

}
