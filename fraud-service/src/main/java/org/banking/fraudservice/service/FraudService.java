package org.banking.fraudservice.service;

import lombok.extern.slf4j.Slf4j;
import org.banking.commonlib.transaction.fraud.FraudTransactionEvent;
import org.banking.fraudservice.model.Fraud;
import org.banking.fraudservice.repository.FraudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FraudService {

    private final FraudRepository fraudRepository;

    public FraudService(FraudRepository fraudRepository) {
        this.fraudRepository = fraudRepository;
    }

    public void logFraud(FraudTransactionEvent event) {
        log.warn("Flagging transaction {} for user {} — reason: {}",
                event.transactionId(), event.userId(), event.fraudSuspicion());

        Fraud fraud = new Fraud(
                event.transactionId(),
                event.userId(),
                event.fraudSuspicion());

        fraudRepository.save(fraud);
    }

    public List<Fraud> getAllFrauds() {
        return fraudRepository.findAll();
    }

    public Optional<Fraud> getFraudById(String transactionId) {
        return fraudRepository.findById(transactionId);
    }

    public List<Fraud> getFraudsByUserId(String userId) {
        return fraudRepository.findAllByUserId(userId);
    }
}
