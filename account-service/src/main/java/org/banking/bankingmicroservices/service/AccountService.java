package org.banking.bankingmicroservices.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.banking.bankingmicroservices.dto.AccountByIdDTO;
import org.banking.bankingmicroservices.dto.TransferDTO;
import org.banking.bankingmicroservices.exception.InsufficientFundsException;
import org.banking.bankingmicroservices.exception.ResourceNotFoundException;
import org.banking.bankingmicroservices.kafka.producer.TransactionEventProducer;
import org.banking.bankingmicroservices.model.Account;
import org.banking.bankingmicroservices.repository.AccountRepository;
import org.banking.commonlib.transaction.AccountDepositEvent;
import org.banking.commonlib.transaction.AccountTransferEvent;
import org.banking.commonlib.transaction.AccountWithdrawEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionEventProducer eventProducer;

    public AccountService(
            AccountRepository accountRepository,
            TransactionEventProducer eventProducer) {
        this.accountRepository = accountRepository;
        this.eventProducer = eventProducer;
    }

    public Account createAccount(String userId) {
        Account existingAccount = accountRepository.findById(userId).orElse(null);
        if (existingAccount != null) {
            log.info("Account already exists for user: {}", userId);
            return existingAccount;
        }

        Account account = new Account(userId, new BigDecimal(1000), LocalDate.now());
        accountRepository.save(account);
        log.info("Created new account for user: {}", userId);

        return account;
    }

    public Account getAccount(String username) {
        return accountRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + username));
    }

    public AccountByIdDTO getAccountById(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));

        return new AccountByIdDTO(
                account.getUsername(),
                account.getAccountBalance(),
                account.getCreationDate());
    }

    public void deposit(String username, BigDecimal amount) {
        Account account = accountRepository.findById(username).orElseThrow(
                () -> new ResourceNotFoundException("Account not found: " + username));

        account.setAccountBalance(account.getAccountBalance().add(amount));
        accountRepository.save(account);

        log.info("Deposited {} to account {}", amount, username);
        publishDepositEvent(account, amount);
    }

    public void withdraw(String username, BigDecimal amount) {
        Account account = accountRepository.findById(username).orElseThrow(
                () -> new ResourceNotFoundException("Account not found: " + username));

        if (account.getAccountBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient Funds");
        }

        account.setAccountBalance(account.getAccountBalance().subtract(amount));
        accountRepository.save(account);

        log.info("Withdrew {} from account {}", amount, username);
        publishWithdrawEvent(account, amount);
    }

    @Transactional
    public void transfer(String username, @Valid TransferDTO transferDTO) {
        if (username.equals(transferDTO.recipientUsername())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        Account account = accountRepository.findById(username).orElseThrow(
                () -> new ResourceNotFoundException("Account not found: " + username));
        Account recipientAccount = accountRepository.findById(transferDTO.recipientUsername()).orElseThrow(
                () -> new ResourceNotFoundException("Account not found: " + transferDTO.recipientUsername()));

        if (account.getAccountBalance().subtract(transferDTO.amount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient Funds");
        }

        account.setAccountBalance(account.getAccountBalance().subtract(transferDTO.amount()));
        recipientAccount.setAccountBalance(recipientAccount.getAccountBalance().add(transferDTO.amount()));
        accountRepository.save(account);
        accountRepository.save(recipientAccount);

        log.info("Transferred {} from {} to {}", transferDTO.amount(), username, transferDTO.recipientUsername());
        publishTransferEvent(account, recipientAccount, transferDTO.amount());
    }

    private void publishDepositEvent(Account account, BigDecimal amount) {
        eventProducer.sendDepositEvent(new AccountDepositEvent(
                account.getUsername(),
                amount,
                LocalDateTime.now()));
    }

    private void publishWithdrawEvent(Account account, BigDecimal amount) {
        eventProducer.sendWithdrawEvent(new AccountWithdrawEvent(
                account.getUsername(),
                amount,
                LocalDateTime.now()));
    }

    private void publishTransferEvent(Account account, Account recipient, BigDecimal amount) {
        eventProducer.sendTransferEvent(new AccountTransferEvent(
                account.getUsername(),
                recipient.getUsername(),
                amount,
                LocalDateTime.now()));
    }
}
