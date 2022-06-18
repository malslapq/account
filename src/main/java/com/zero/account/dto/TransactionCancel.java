package com.zero.account.dto;

import com.zero.account.aop.AccountLockIdInterface;
import com.zero.account.type.TransactionResultStatus;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransactionCancel {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request implements AccountLockIdInterface {

        @NotNull
        private Long transactionId;
        @NotBlank
        private String accountNumber;
        @NotNull
        @Max(value = 1000_000_000)
        @Min(value = 100, message = "최소 거래 금액보다 작습니다. (최소 거래 금액 100원)")
        private Long transactionAmount;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {

        private String accountNumber;
        private TransactionResultStatus transactionResultStatus;
        private Long transactionId;
        private Long transactionAmount;
        private LocalDateTime createdAt;

        public static Response from(TransactionDto transactionDto, String accountNumber) {
            return Response.builder()
                    .accountNumber(accountNumber)
                    .transactionResultStatus(transactionDto.getTransactionResultStatus())
                    .transactionId(transactionDto.getTransactionId())
                    .transactionAmount(transactionDto.getTransactionAmount())
                    .createdAt(transactionDto.getCreatedAt())
                    .build();
        }

    }


}
