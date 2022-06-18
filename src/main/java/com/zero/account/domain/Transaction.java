package com.zero.account.domain;

import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    @Enumerated(EnumType.STRING)
    private TransactionResultStatus transactionResultStatus;
    private Long transactionAmount;
    private String accountNumber;


    @CreatedDate
    private LocalDateTime createdAt;

    public Account getAccount() {
        return account;
    }


}


