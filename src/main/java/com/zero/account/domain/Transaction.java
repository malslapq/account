package com.zero.account.domain;

import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;


@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Transaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    @Enumerated(EnumType.STRING)
    private TransactionResultStatus transactionResultStatus;
    private Long transactionAmount;
    private String accountNumber;

}


