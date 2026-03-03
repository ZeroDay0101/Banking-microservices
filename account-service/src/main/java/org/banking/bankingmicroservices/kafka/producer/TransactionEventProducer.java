package org.banking.bankingmicroservices.kafka.producer;

import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.AccountTransferEvent;
import org.banking.commonlib.transaction.AccountWithdrawEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String depositTopic;
    private final String withdrawTopic;
    private final String transferTopic;

    public TransactionEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${banking.kafka.topics.deposit}") String depositTopic,
            @Value("${banking.kafka.topics.withdraw}") String withdrawTopic,
            @Value("${banking.kafka.topics.transfer}") String transferTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.depositTopic = depositTopic;
        this.withdrawTopic = withdrawTopic;
        this.transferTopic = transferTopic;
    }

    public void sendDepositEvent(AccountDepositEvent event) {
        kafkaTemplate.send(depositTopic, event);
    }

    public void sendWithdrawEvent(AccountWithdrawEvent event) {
        kafkaTemplate.send(withdrawTopic, event);
    }

    public void sendTransferEvent(AccountTransferEvent event) {
        kafkaTemplate.send(transferTopic, event);
    }
}
