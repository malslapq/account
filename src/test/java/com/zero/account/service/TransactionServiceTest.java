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
import com.zero.account.type.ErrorCode;
import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;


    @DisplayName("거래(결제) -  성공")
    @Test
    void successTransactionUse() {
        // given
        Long setUserId = 1L;
        Long setAccountId = 1L;
        Long setTransactionId = 1L;
        String setAccountNumber = "12345";
        Long setBalance = 10000L;
        Long paymentAmount = 1000L;
        given(accountUserRepository.findById(any())).willReturn(Optional.of(
                AccountUser.builder()
                        .id(setUserId)
                        .build())
        );
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(setAccountId)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(setBalance)
                        .accountNumber(setAccountNumber)
                        .accountUser(AccountUser.builder()
                                .id(setUserId)
                                .build())
                        .build()));
        given(transactionRepository.save(any())).willReturn(
                Transaction.builder()
                        .id(setTransactionId)
                        .account(Account.builder()
                                .id(setAccountId)
                                .accountStatus(AccountStatus.IN_USE)
                                .balance(setBalance)
                                .accountNumber(setAccountNumber)
                                .accountUser(AccountUser.builder()
                                        .id(setUserId)
                                        .build())
                                .build())
                        .transactionStatus(TransactionStatus.APPROVAL)
                        .transactionAmount(paymentAmount)
                        .accountNumber(setAccountNumber)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // when
        TransactionDto transaction = transactionService.transactionUse(setUserId,
                setAccountNumber, paymentAmount);


        // then
        assertThat(transaction.getAccountNumber()).isEqualTo(setAccountNumber);
        assertThat(transaction.getTransactionStatus()).isEqualTo(TransactionStatus.APPROVAL);
        assertThat(transaction.getTransactionAmount()).isEqualTo(paymentAmount);

    }

    @DisplayName("거래(결제) - 실패 사용자 없음")
    @Test
    void failedCreateTransactionNotFoundUser() {
        // given

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 1L));


        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getDescription());
    }

    @DisplayName("거래(결제) - 실패 계좌 없음")
    @Test
    void failedCreateTransactionNotFoundAccount() {
        // given
        given(accountUserRepository.findById(any())).willReturn(Optional.of(
                AccountUser.builder()
                        .id(1L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 1L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getDescription());
    }

    @DisplayName("거래(결제) - 실패 계좌 소유주 다름")
    @Test
    void failedCreateTransactionMissMatchAccountUser() {
        // given
        given(accountUserRepository.findById(any())).willReturn(Optional.of(
                AccountUser.builder()
                        .id(1L)
                        .build()
        ));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .accountUser(AccountUser.builder().id(2L).build())
                        .accountStatus(AccountStatus.IN_USE)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 1L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.USER_MIS_MATCH.getDescription());
    }

    @DisplayName("거래(결제) - 실패 해지된 계좌")
    @Test
    void failedCreateTransactionCancelAccount() {
        // given
        AccountUser accountUser = AccountUser.builder().id(1L).build();
        given(accountUserRepository.findById(any())).willReturn(Optional.ofNullable(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 1L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.CANCEL_ACCOUNT.getDescription());
    }

    @DisplayName("거래(결제) - 실패 잔액 부족")
    @Test
    void failedCreateTransactionInsufficientBalance() {
        // given
        AccountUser accountUser = AccountUser.builder().id(1L).build();
        given(accountUserRepository.findById(any())).willReturn(Optional.ofNullable(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 10000L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE.getDescription());
    }

    @DisplayName("거래(결제) - 실패 최소 거래 금액 미만")
    @Test
    void failedCreateTransactionOutOfRangeMinimumBalance() {
        // given
        AccountUser accountUser = AccountUser.builder().id(1L).build();
        given(accountUserRepository.findById(any())).willReturn(Optional.ofNullable(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 10L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.USE_BALANCE_MINIMUM_OUT_OF_RANGE.getDescription());
    }

    @DisplayName("거래(결제) - 실패 최대 거래 금액 초과")
    @Test
    void failedCreateTransactionOutOfRangeMaximumBalance() {
        // given
        AccountUser accountUser = AccountUser.builder().id(1L).build();
        given(accountUserRepository.findById(any())).willReturn(Optional.ofNullable(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 100_000_000L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.USE_BALANCE_MAXIMUM_OUT_OF_RANGE.getDescription());
    }


    @DisplayName("거래(취소) - 실패 금액이 다름")
    @Test
    void failedTransactionCancelMissMatchTransactionAmount() {
        // given
        given(transactionRepository.findById(any())).willReturn(Optional.of(
                Transaction.builder()
                        .id(1L)
                        .accountNumber("123")
                        .transactionAmount(1000L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionCancel(1L, "123", 2000L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.TRANSACTION_AMOUNT_MIS_MATCH.getDescription());
    }

    @DisplayName("거래(취소) - 실패 해당 계좌의 거래가 아님")
    @Test
    void failedTransactionCancelMissMatchTransaction() {
        // given
        given(transactionRepository.findById(any())).willReturn(Optional.of(
                Transaction.builder()
                        .id(1L)
                        .accountNumber("123")
                        .transactionAmount(1000L)
                        .build()
        ));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionCancel(1L, "12345", 2000L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.TRANSACTION_ACCOUNT_NUMBER_MIS_MATCH.getDescription());
    }

    @DisplayName("거래(취소) 성공")
    @Test
    void successTransactionCancel() {
        // given
        given(transactionRepository.findById(any())).willReturn(Optional.of(
                Transaction.builder()
                        .id(1L)
                        .accountNumber("123")
                        .transactionAmount(2000L)
                        .account(Account.builder()
                                .id(1L)
                                .balance(5000L)
                                .accountUser(AccountUser.builder()
                                        .id(1L)
                                        .build())
                                .accountStatus(AccountStatus.IN_USE)
                                .build())
                        .build()
        ));
        given(transactionRepository.save(any())).willReturn(Transaction.builder()
                .id(1L)
                .accountNumber("123")
                .transactionAmount(2000L)
                .account(Account.builder()
                        .id(1L)
                        .balance(5000L)
                        .accountUser(AccountUser.builder()
                                .id(1L)
                                .build())
                        .accountStatus(AccountStatus.IN_USE)
                        .build())
                .transactionStatus(TransactionStatus.CANCEL)
                .build());

        // when
        TransactionDto transactionDto = transactionService.transactionCancel(
                1L, "123", 2000L);

        // then
        assertThat(transactionDto.getTransactionAmount()).isEqualTo(2000L);
        assertThat(transactionDto.getTransactionStatus()).isEqualTo(TransactionStatus.CANCEL);
        assertThat(transactionDto.getAccountNumber()).isEqualTo("123");
        assertThat(transactionDto.getTransactionId()).isEqualTo(1L);
    }

    @DisplayName("거래 확인 - 실패 존재하지 않는 거래")
    @Test
    void failedSelectTransactionNotFound() {
        // given


        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                transactionService.selectTransaction(any()));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND.getDescription());

    }

    @DisplayName("거래 확인 성공")
    @Test
    void successSelectTransaction() {
        // given
        given(transactionRepository.findById(any())).willReturn(Optional.of(
                Transaction.builder()
                        .id(1L)
                        .transactionResultStatus(TransactionResultStatus.SUCCEED)
                        .transactionAmount(1000L)
                        .accountNumber("12345")
                        .createdAt(LocalDateTime.now())
                        .transactionStatus(TransactionStatus.APPROVAL)
                        .build()
        ));

        // when
        TransactionInfo transactionInfo = transactionService.selectTransaction(1L);

        // then
        assertThat(transactionInfo.getTransactionId()).isEqualTo(1L);
        assertThat(transactionInfo.getAccountNumber()).isEqualTo("12345");
        assertThat(transactionInfo.getTransactionStatus()).isEqualTo(TransactionStatus.APPROVAL);
        assertThat(transactionInfo.getTransactionResultStatus()).isEqualTo(TransactionResultStatus.SUCCEED);
        assertThat(transactionInfo.getTransactionAmount()).isEqualTo(1000L);
    }

}