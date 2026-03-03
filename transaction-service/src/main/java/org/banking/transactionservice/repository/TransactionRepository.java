package org.banking.transactionservice.repository;

import org.banking.transactionservice.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByRecipientId(String recipientId);

    long countByUserIdAndDateGreaterThanEqual(String userId, LocalDateTime since);
}
