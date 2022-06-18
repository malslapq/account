package com.zero.account.dto;

import com.zero.account.aop.AccountLockIdInterface;
import com.zero.account.type.TransactionResultStatus;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransactionUse {

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request implements AccountLockIdInterface {

        @NotNull
        private Long userId;
        @NotBlank
        private String accountNumber;
        @NotNull
        private Long transactionAmount;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {

        private Long userId;
        private String accountNumber;
        private TransactionResultStatus transactionResultStatus;
        private Long transactionAmount;
        private LocalDateTime createdAt;

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResultStatus(transactionDto.getTransactionResultStatus())
                    .userId(transactionDto.getTransactionId())
                    .transactionAmount(transactionDto.getTransactionAmount())
                    .createdAt(transactionDto.getCreatedAt())
                    .build();
        }

    }


}
