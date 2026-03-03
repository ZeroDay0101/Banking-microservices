package org.banking.transactionservice.kafka.consumer;

import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.AccountTransferEvent;
import org.banking.commonlib.transaction.AccountWithdrawEvent;
import org.banking.transactionservice.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private final TransactionService transactionService;

    public TransactionEventConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${banking.kafka.topics.deposit}", groupId = "account-transactions-consumer", concurrency = "2")
    public void listenDeposit(AccountDepositEvent depositEvent) {
        transactionService.saveDepositTransaction(depositEvent);
    }

    @KafkaListener(topics = "${banking.kafka.topics.withdraw}", groupId = "account-transactions-consumer")
    public void listenWithdraw(AccountWithdrawEvent withdrawEvent) {
        transactionService.saveWithdrawTransaction(withdrawEvent);
    }

    @KafkaListener(topics = "${banking.kafka.topics.transfer}", groupId = "account-transactions-consumer", concurrency = "2")
    public void listenTransfer(AccountTransferEvent transferEvent) {
        transactionService.saveTransferTransaction(transferEvent);
    }

}
