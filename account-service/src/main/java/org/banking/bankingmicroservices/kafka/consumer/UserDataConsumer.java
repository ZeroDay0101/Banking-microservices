package org.banking.bankingmicroservices.kafka.consumer;

import org.banking.bankingmicroservices.service.AccountService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserDataConsumer {
    private final AccountService accountService;

    public UserDataConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = "user-create-account", groupId = "user-data-consumer")
    public void listen(String username) {
        accountService.createAccount(username);
    }
}
