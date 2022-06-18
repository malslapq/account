package com.zero.account.service;

import com.zero.account.domain.Account;
import com.zero.account.domain.AccountUser;
import com.zero.account.domain.Transaction;
import com.zero.account.dto.TransactionDto;
import com.zero.account.dto.TransactionInfo;
import com.zero.account.exception.AccountException;
import com.zero.account.repository.AccountRepository;
import com.zero.account.repository.AccountUserRepository;
import com.zero.account.repository.TransactionRepository;
import com.zero.account.type.AccountStatus;
import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

import static com.zero.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private Transaction saveGetTransaction(String accountNumber,
                                           Long paymentAmount,
                                           Account account,
                                           TransactionResultStatus transactionResultStatus,
                                           TransactionStatus transactionStatus) {
        return transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .transactionResultStatus(transactionResultStatus)
                        .transactionStatus(transactionStatus)
                        .transactionAmount(paymentAmount)
                        .accountNumber(String.valueOf(accountNumber))
                        .build());
    }

    @Transactional
    public void createFailedTransaction(
            String accountNumber, Long paymentAmount, TransactionStatus transactionStatus) {
        Account account = accountRepository.findByAccountNumber(String.valueOf(accountNumber))
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        Transaction transaction = saveGetTransaction(
                accountNumber,
                paymentAmount,
                account,
                TransactionResultStatus.FAILED,
                transactionStatus);
        System.out.println(transaction);
    }


    private void validateTransactionUse(AccountUser accountUser, Account account) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_MIS_MATCH);
        }
        if (account.getAccountStatus().equals(AccountStatus.UNREGISTERED)) {
            throw new AccountException(CANCEL_ACCOUNT);
        }
    }

    @Transactional
    public TransactionDto transactionUse(Long userId, String accountNumber, Long paymentAmount) {
        AccountUser accountUser =
                accountUserRepository.findById(userId).orElseThrow(() ->
                        new AccountException(USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(String.valueOf(accountNumber))
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateTransactionUse(accountUser, account);
        account.useBalance(paymentAmount);
        accountRepository.save(account);

        return TransactionDto.fromEntity(
                saveGetTransaction(
                        accountNumber,
                        paymentAmount,
                        account,
                        TransactionResultStatus.SUCCEED,
                        TransactionStatus.APPROVAL));
    }


    @Transactional
    public TransactionDto transactionCancel(Long transactionId, String accountNumber,
                                            Long cancellationAmount) {
        Transaction transaction =
                transactionRepository.findById(transactionId).orElseThrow(() ->
                        new AccountException(TRANSACTION_NOT_FOUND));
        if (!Long.valueOf(transaction.getAccountNumber()).equals(Long.valueOf(accountNumber))) {
            throw new AccountException(TRANSACTION_ACCOUNT_NUMBER_MIS_MATCH);
        }
        if (!Objects.equals(transaction.getTransactionAmount(), cancellationAmount)) {
            throw new AccountException(TRANSACTION_AMOUNT_MIS_MATCH);
        }

        Account account = transaction.getAccount();

        account.setBalance(account.getBalance() + cancellationAmount);
        accountRepository.save(account);
        return TransactionDto.fromEntity(
                saveGetTransaction(
                        accountNumber,
                        cancellationAmount,
                        account,
                        TransactionResultStatus.SUCCEED,
                        TransactionStatus.CANCEL));
    }

    @Transactional
    public TransactionInfo selectTransaction(Long transactionId) {
        Transaction transaction =
                transactionRepository.findById(transactionId).orElseThrow(() ->
                        new AccountException(TRANSACTION_NOT_FOUND));

        return TransactionInfo.builder()
                .transactionId(transaction.getId())
                .accountNumber(transaction.getAccountNumber())
                .transactionAmount(transaction.getTransactionAmount())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionResultStatus(transaction.getTransactionResultStatus())
                .registeredAt(transaction.getCreatedAt())
                .build();
    }


}
