package com.zero.account.dto;

import com.zero.account.domain.Transaction;
import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TransactionDto {

    private Long transactionId;
    private String accountNumber;
    private TransactionStatus transactionStatus;
    private TransactionResultStatus transactionResultStatus;
    private Long transactionAmount;
    private LocalDateTime createdAt;

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .transactionId(transaction.getId())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionResultStatus(transaction.getTransactionResultStatus())
                .transactionAmount(transaction.getTransactionAmount())
                .accountNumber(transaction.getAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
