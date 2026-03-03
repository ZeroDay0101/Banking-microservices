package org.banking.commonlib.transaction.fraud;

public record FraudTransactionEvent
        (
                String transactionId,
                String userId,
                FraudSuspicion fraudSuspicion
        ) {
}
