package org.banking.bankingmicroservices.repository;

import org.banking.bankingmicroservices.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
}
