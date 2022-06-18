package com.zero.account.dto;

import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionInfo {

    private Long transactionId;
    private String accountNumber;
    private TransactionStatus transactionStatus;
    private TransactionResultStatus transactionResultStatus;
    private Long transactionAmount;
    private LocalDateTime registeredAt;

}
