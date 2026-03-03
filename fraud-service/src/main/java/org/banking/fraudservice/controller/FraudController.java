package org.banking.fraudservice.controller;

import org.banking.fraudservice.model.Fraud;
import org.banking.fraudservice.service.FraudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudService fraudService;

    public FraudController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping
    public ResponseEntity<List<Fraud>> getAllFrauds() {
        return ResponseEntity.ok(fraudService.getAllFrauds());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Fraud> getFraudById(@PathVariable String transactionId) {
        return fraudService.getFraudById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Fraud>> getFraudsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(fraudService.getFraudsByUserId(userId));
    }
}
