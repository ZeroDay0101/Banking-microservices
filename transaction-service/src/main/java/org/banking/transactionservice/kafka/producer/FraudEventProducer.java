package org.banking.transactionservice.kafka.producer;

import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FraudEventProducer {
    private final KafkaTemplate<String, FraudTransactionEvent> kafkaTemplate;
    private final String fraudTopic;

    public FraudEventProducer(
            KafkaTemplate<String, FraudTransactionEvent> kafkaTemplate,
            @Value("${banking.kafka.topics.fraud}") String fraudTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.fraudTopic = fraudTopic;
    }

    public void sendFraudEvent(FraudTransactionEvent event) {
        kafkaTemplate.send(fraudTopic, event);
    }

}
