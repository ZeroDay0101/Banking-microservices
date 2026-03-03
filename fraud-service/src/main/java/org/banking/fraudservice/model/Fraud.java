package org.banking.fraudservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.banking.commonlib.transaction.fraud.FraudSuspicion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("fraud")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Fraud {
    @Id
    private String transactionId;

    private String userId;

    private FraudSuspicion fraudSuspicion;


}
