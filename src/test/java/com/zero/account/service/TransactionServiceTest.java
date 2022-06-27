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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder()
                .accountNumber("12345")
                .balance(10000L)
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .build();
        account.setId(1L);
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionStatus(TransactionStatus.APPROVAL)
                .transactionAmount(1000L)
                .accountNumber("12345")
                .build();
        transaction.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account));
        given(transactionRepository.save(any())).willReturn(transaction);

        // when
        TransactionDto getTransaction = transactionService.transactionUse(
                1L, "12345", 1000L);


        // then
        assertThat(getTransaction.getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(getTransaction.getTransactionStatus()).isEqualTo(TransactionStatus.APPROVAL);
        assertThat(getTransaction.getTransactionAmount()).isEqualTo(transaction.getTransactionAmount());

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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));

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
        AccountUser accountUser1 = new AccountUser();
        accountUser1.setId(1L);
        AccountUser accountUser2 = new AccountUser();
        accountUser2.setId(2L);
        Account account = Account.builder()
                .accountUser(accountUser2)
                .accountStatus(AccountStatus.IN_USE)
                .build();
        account.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser1));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account));

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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder().accountUser(accountUser).accountStatus(AccountStatus.UNREGISTERED).build();
        account.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account
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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(1000L)
                        .build();
        account.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account));

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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .build();
        account.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account));

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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .build();
        account.setId(1L);
        given(accountUserRepository.findById(any())).willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any())).willReturn(Optional.of(account));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.transactionUse(1L, "1", 100_000_000L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ErrorCode.USE_BALANCE_MAXIMUM_OUT_OF_RANGE.getDescription());
    }


    @DisplayName("거래(취소) - 실패 거래 금액이 다름")
    @Test
    void failedTransactionCancelMissMatchTransactionAmount() {
        // given
        Transaction transaction = Transaction.builder()
                .accountNumber("123")
                .transactionAmount(1000L)
                .build();
        transaction.setId(1L);
        given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));

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
        Transaction transaction = Transaction.builder()
                .accountNumber("123")
                .transactionAmount(1000L)
                .build();
        transaction.setId(1L);
        given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));

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
        AccountUser accountUser = new AccountUser();
        accountUser.setId(1L);
        Account account = Account.builder()
                .balance(5000L)
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .build();
        account.setId(1L);
        Transaction transaction1 = Transaction.builder()
                .accountNumber("123")
                .transactionAmount(2000L)
                .account(account)
                .build();
        transaction1.setId(1L);
        Transaction transaction2 = Transaction.builder()
                .accountNumber("123")
                .transactionAmount(2000L)
                .account(account)
                .transactionStatus(TransactionStatus.CANCEL)
                .build();
        transaction2.setId(1L);
        given(transactionRepository.findById(any())).willReturn(Optional.of(transaction1));
        given(transactionRepository.save(any())).willReturn(transaction2);

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
        Transaction transaction = Transaction.builder()
                .transactionResultStatus(TransactionResultStatus.SUCCEED)
                .transactionAmount(1000L)
                .accountNumber("12345")
                .transactionStatus(TransactionStatus.APPROVAL)
                .build();
        transaction.setId(1L);
        given(transactionRepository.findById(any())).willReturn(Optional.of(transaction));

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