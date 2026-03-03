package org.banking.transactionservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
@CompoundIndex(name = "userId_date_idx", def = "{'userId': 1, 'date': -1}")
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Transaction {
    @Id
    private String id;

    private String userId;

    @Indexed
    private String recipientId;

    private BigDecimal amount;

    private LocalDateTime date;

    private TransactionType type;

    public Transaction(String userId, String recipientId, BigDecimal amount, LocalDateTime date, TransactionType type) {
        this.userId = userId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }
}
