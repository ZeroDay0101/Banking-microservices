package org.banking.transactionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.AccountTransferEvent;
import org.banking.commonlib.transaction.AccountWithdrawEvent;
import org.banking.commonlib.transaction.fraud.FraudSuspicion;
import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.banking.transactionservice.dto.TransactionOutDTO;
import org.banking.transactionservice.kafka.producer.FraudEventProducer;
import org.banking.transactionservice.model.Transaction;
import org.banking.transactionservice.model.TransactionType;
import org.banking.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final FraudEventProducer fraudEventProducer;
    private final BigDecimal fraudAmountThreshold;
    private final int fraudFrequencyThreshold;
    private final int fraudFrequencyWindowMinutes;

    public TransactionService(
            TransactionRepository transactionRepository,
            FraudEventProducer fraudEventProducer,
            @Value("${banking.fraud.amount-threshold:10000}") BigDecimal fraudAmountThreshold,
            @Value("${banking.fraud.frequency-threshold:5}") int fraudFrequencyThreshold,
            @Value("${banking.fraud.frequency-window-minutes:1}") int fraudFrequencyWindowMinutes) {
        this.transactionRepository = transactionRepository;
        this.fraudEventProducer = fraudEventProducer;
        this.fraudAmountThreshold = fraudAmountThreshold;
        this.fraudFrequencyThreshold = fraudFrequencyThreshold;
        this.fraudFrequencyWindowMinutes = fraudFrequencyWindowMinutes;
    }

    public void saveDepositTransaction(AccountDepositEvent event) {
        log.info("Recording deposit of {} for account {}", event.amount(), event.accountId());
        saveTransaction(
                event.accountId(),
                null,
                event.amount(),
                event.occurredAt(),
                TransactionType.DEPOSIT);
    }

    public void saveWithdrawTransaction(AccountWithdrawEvent event) {
        log.info("Recording withdrawal of {} for account {}", event.amount(), event.accountId());
        saveTransaction(
                event.accountId(),
                null,
                event.amount(),
                event.occurredAt(),
                TransactionType.WITHDRAW);
    }

    public void saveTransferTransaction(AccountTransferEvent event) {
        log.info("Recording transfer of {} from {} to {}", event.amount(), event.accountId(), event.recipientId());
        saveTransaction(
                event.accountId(),
                event.recipientId(),
                event.amount(),
                event.occurredAt(),
                TransactionType.TRANSFER);
    }

    public Optional<TransactionOutDTO> getTransactionById(String id) {
        return transactionRepository.findById(id)
                .map(this::toTransactionOutDTO);
    }

    public List<TransactionOutDTO> getAllTransactionsByUserId(String userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(this::toTransactionOutDTO)
                .toList();
    }

    public List<TransactionOutDTO> getAllTransactionsByRecipientId(String recipientId) {
        return transactionRepository.findByRecipientId(recipientId)
                .stream()
                .map(this::toTransactionOutDTO)
                .toList();
    }

    private TransactionOutDTO toTransactionOutDTO(Transaction transaction) {
        return new TransactionOutDTO(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getRecipientId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType());
    }

    private void saveTransaction(
            String accountId,
            String recipientId,
            BigDecimal amount,
            LocalDateTime occurredAt,
            TransactionType type) {
        Transaction transaction = new Transaction(accountId, recipientId, amount, occurredAt, type);
        transactionRepository.save(transaction);

        processPossibleFraudEvent(transaction);
    }

    private void processPossibleFraudEvent(Transaction transaction) {

        LocalDateTime referenceTime = transaction.getDate() != null ? transaction.getDate() : LocalDateTime.now();
        LocalDateTime windowStart = referenceTime.minusMinutes(fraudFrequencyWindowMinutes);

        long count = transactionRepository.countByUserIdAndDateGreaterThanEqual(transaction.getUserId(), windowStart);

        boolean fraudAmountTriggered = transaction.getAmount().compareTo(fraudAmountThreshold) > 0;

        FraudSuspicion fraudSuspicion;

        if (count >= fraudFrequencyThreshold)
            fraudSuspicion = FraudSuspicion.TOO_FREQUENT_TRANSACTIONS;
        else if (fraudAmountTriggered)
            fraudSuspicion = FraudSuspicion.HIGH_AMOUNT_TRANSACTION;
        else
            return;

        log.warn("Fraud suspicion detected for user {}: {}", transaction.getUserId(), fraudSuspicion);
        fraudEventProducer.sendFraudEvent(
                new FraudTransactionEvent(
                        transaction.getId(),
                        transaction.getUserId(),
                        fraudSuspicion));
    }

}
