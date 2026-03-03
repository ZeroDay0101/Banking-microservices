package org.banking.bankingmicroservices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountByIdDTO(
        String id,
        BigDecimal accountBalance,
        LocalDate creationDate
) {
}
