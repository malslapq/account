package com.zero.account.dto;


import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PatchAccount {

    @Getter
    @Setter
    public static class Request {

        @NotNull
        @Min(1)
        private Long userId;
        @NotBlank
        private String accountNumber;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {

        private Long userId;
        private String accountNumber;
        private LocalDateTime updatedAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .updatedAt(accountDto.getUnRegisteredAt())
                    .build();
        }

    }


}
