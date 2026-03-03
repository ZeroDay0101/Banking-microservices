package org.banking.bankingmicroservices.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "accounts")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    private String username;

    private BigDecimal accountBalance;

    private LocalDate creationDate;
}
