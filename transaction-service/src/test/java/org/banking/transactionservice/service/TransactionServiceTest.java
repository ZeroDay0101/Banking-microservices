package org.banking.transactionservice.service;

import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.banking.transactionservice.kafka.producer.FraudEventProducer;
import org.banking.transactionservice.model.Transaction;
import org.banking.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudEventProducer fraudEventProducer;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                fraudEventProducer,
                BigDecimal.valueOf(10000),
                5,
                1);
    }

    @Test
    void shouldUseEventTimeForFraudWindow() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 2, 26, 10, 0, 0);
        AccountDepositEvent event = new AccountDepositEvent("user-1", BigDecimal.valueOf(100), occurredAt);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.countByUserIdAndDateGreaterThanEqual(eq("user-1"), any(LocalDateTime.class))).thenReturn(5L);

        transactionService.saveDepositTransaction(event);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(transactionRepository).countByUserIdAndDateGreaterThanEqual(eq("user-1"), sinceCaptor.capture());
        assertEquals(occurredAt.minusMinutes(1), sinceCaptor.getValue());
    }

    @Test
    void shouldNotSendFraudEventForNormalTransaction() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 2, 26, 11, 0, 0);
        AccountDepositEvent event = new AccountDepositEvent("user-2", BigDecimal.valueOf(50), occurredAt);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.countByUserIdAndDateGreaterThanEqual(eq("user-2"), any(LocalDateTime.class))).thenReturn(1L);

        transactionService.saveDepositTransaction(event);

        verify(fraudEventProducer, never()).sendFraudEvent(any(FraudTransactionEvent.class));
    }
}
