package org.banking.fraudservice.kafka.consumer;

import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.banking.fraudservice.service.FraudService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudEventConsumer {

    private final FraudService fraudService;

    public FraudEventConsumer(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @KafkaListener(topics = "${banking.kafka.topics.fraud}", groupId = "transactions-fraud-consumer")
    public void consumeFraudEvent(FraudTransactionEvent event) {
        fraudService.logFraud(event);
    }
}
