package org.banking.bankingmicroservices.service;

import org.banking.bankingmicroservices.dto.TransferDTO;
import org.banking.bankingmicroservices.exception.InsufficientFundsException;
import org.banking.bankingmicroservices.exception.ResourceNotFoundException;
import org.banking.bankingmicroservices.kafka.producer.TransactionEventProducer;
import org.banking.bankingmicroservices.model.Account;
import org.banking.bankingmicroservices.repository.AccountRepository;
import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.AccountTransferEvent;
import org.banking.commonlib.transaction.AccountWithdrawEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionEventProducer transactionEventProducer;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldBeIdempotentForDuplicateAccountCreation() {
        Account existing = new Account("user-1", null, null);
        when(accountRepository.findById("user-1")).thenReturn(Optional.of(existing));
        Account result = accountService.createAccount("user-1");

        assertEquals(existing, result);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any(Account.class));
    }

    @Test
    void depositShouldIncreaseBalanceAndPublishEvent() {
        Account account = new Account("user-1", BigDecimal.valueOf(1000), LocalDate.now());
        when(accountRepository.findById("user-1")).thenReturn(Optional.of(account));

        accountService.deposit("user-1", BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(1200), account.getAccountBalance());
        verify(accountRepository).save(account);

        ArgumentCaptor<AccountDepositEvent> captor = ArgumentCaptor.forClass(AccountDepositEvent.class);
        verify(transactionEventProducer).sendDepositEvent(captor.capture());
        AccountDepositEvent event = captor.getValue();
        assertEquals("user-1", event.accountId());
        assertEquals(BigDecimal.valueOf(200), event.amount());
    }

    @Test
    void depositShouldThrowWhenAccountNotFound() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.deposit("missing", BigDecimal.TEN));

        verify(transactionEventProducer, never()).sendDepositEvent(any(AccountDepositEvent.class));
    }

    @Test
    void withdrawShouldDecreaseBalanceAndPublishEvent() {
        Account account = new Account("user-1", BigDecimal.valueOf(500), LocalDate.now());
        when(accountRepository.findById("user-1")).thenReturn(Optional.of(account));

        accountService.withdraw("user-1", BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(300), account.getAccountBalance());
        verify(accountRepository).save(account);

        ArgumentCaptor<AccountWithdrawEvent> captor = ArgumentCaptor.forClass(AccountWithdrawEvent.class);
        verify(transactionEventProducer).sendWithdrawEvent(captor.capture());
        AccountWithdrawEvent event = captor.getValue();
        assertEquals("user-1", event.accountId());
        assertEquals(BigDecimal.valueOf(200), event.amount());
    }

    @Test
    void withdrawShouldThrowWhenInsufficientFunds() {
        Account account = new Account("user-1", BigDecimal.valueOf(100), LocalDate.now());
        when(accountRepository.findById("user-1")).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw("user-1", BigDecimal.valueOf(200)));

        verify(transactionEventProducer, never()).sendWithdrawEvent(any(AccountWithdrawEvent.class));
        verify(accountRepository, never()).save(account);
    }

    @Test
    void transferShouldMoveMoneyBetweenAccountsAndPublishEvent() {
        Account sender = new Account("sender", BigDecimal.valueOf(500), LocalDate.now());
        Account recipient = new Account("recipient", BigDecimal.valueOf(100), LocalDate.now());

        when(accountRepository.findById("sender")).thenReturn(Optional.of(sender));
        when(accountRepository.findById("recipient")).thenReturn(Optional.of(recipient));

        TransferDTO dto = new TransferDTO("recipient", BigDecimal.valueOf(200));

        accountService.transfer("sender", dto);

        assertEquals(BigDecimal.valueOf(300), sender.getAccountBalance());
        assertEquals(BigDecimal.valueOf(300), recipient.getAccountBalance());

        verify(accountRepository, times(2)).save(any(Account.class));

        ArgumentCaptor<AccountTransferEvent> captor = ArgumentCaptor.forClass(AccountTransferEvent.class);
        verify(transactionEventProducer).sendTransferEvent(captor.capture());
        AccountTransferEvent event = captor.getValue();
        assertEquals("sender", event.accountId());
        assertEquals("recipient", event.recipientId());
        assertEquals(BigDecimal.valueOf(200), event.amount());
    }

    @Test
    void transferShouldThrowWhenSenderHasInsufficientFunds() {
        Account sender = new Account("sender", BigDecimal.valueOf(100), LocalDate.now());
        Account recipient = new Account("recipient", BigDecimal.valueOf(100), LocalDate.now());

        when(accountRepository.findById("sender")).thenReturn(Optional.of(sender));
        when(accountRepository.findById("recipient")).thenReturn(Optional.of(recipient));

        TransferDTO dto = new TransferDTO("recipient", BigDecimal.valueOf(200));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer("sender", dto));

        verify(transactionEventProducer, never()).sendTransferEvent(any(AccountTransferEvent.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transferShouldThrowWhenRecipientNotFound() {
        Account sender = new Account("sender", BigDecimal.valueOf(500), LocalDate.now());

        when(accountRepository.findById("sender")).thenReturn(Optional.of(sender));
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        TransferDTO dto = new TransferDTO("missing", BigDecimal.valueOf(100));

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.transfer("sender", dto));

        verify(transactionEventProducer, never()).sendTransferEvent(any(AccountTransferEvent.class));
    }
}
