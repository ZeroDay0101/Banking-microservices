package org.banking.transactionservice.kafka.producer;

import org.banking.commonlib.transaction.fraud.FraudSuspicion;
import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudEventProducerTest {

    @Mock
    private KafkaTemplate<String, FraudTransactionEvent> kafkaTemplate;

    @Test
    void shouldSendToConfiguredTopic() {
        FraudEventProducer producer = new FraudEventProducer(kafkaTemplate, "fraud-topic");
        FraudTransactionEvent event = new FraudTransactionEvent("tx-1", "user-1", FraudSuspicion.HIGH_AMOUNT_TRANSACTION);

        producer.sendFraudEvent(event);

        verify(kafkaTemplate).send("fraud-topic", event);
    }
}
