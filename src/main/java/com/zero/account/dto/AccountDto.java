package com.zero.account.dto;

import com.zero.account.domain.Account;
import com.zero.account.type.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto {

    private Long userId;
    private String accountNumber;
    private AccountStatus accountStatus;
    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;



    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountStatus(account.getAccountStatus())
                .balance(account.getBalance())
                .registeredAt(account.getRegisteredAt())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }

}
