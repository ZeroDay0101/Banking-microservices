package org.banking.transactionservice.controller;

import org.banking.transactionservice.dto.TransactionOutDTO;
import org.banking.transactionservice.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionOutDTO> getTransactionById(@PathVariable("id") String id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionOutDTO>> getAllTransactionsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUserId(userId));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<TransactionOutDTO>> getAllTransactionsByRecipientId(@PathVariable String recipientId) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByRecipientId(recipientId));
    }

}
