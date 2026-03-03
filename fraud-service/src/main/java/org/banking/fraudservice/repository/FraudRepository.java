package org.banking.fraudservice.repository;

import org.banking.fraudservice.model.Fraud;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FraudRepository extends MongoRepository<Fraud, String> {
    List<Fraud> findAllByUserId(String userId);
}
