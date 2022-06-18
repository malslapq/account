package com.zero.account.domain;


import com.zero.account.exception.AccountException;
import com.zero.account.type.AccountStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.zero.account.type.ErrorCode.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "account_user_id")
    private AccountUser accountUser;
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void useBalance(Long paymentAmount) {
        if (paymentAmount < 100) {
            throw new AccountException(USE_BALANCE_MINIMUM_OUT_OF_RANGE);
        }
        if (paymentAmount >= 100_000_000) {
            throw new AccountException(USE_BALANCE_MAXIMUM_OUT_OF_RANGE);
        }
        if (paymentAmount > balance) {
            throw new AccountException(INSUFFICIENT_BALANCE);
        }
        this.balance -= paymentAmount;
    }

}
