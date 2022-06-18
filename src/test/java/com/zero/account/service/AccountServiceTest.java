package com.zero.account.service;

import com.zero.account.domain.Account;
import com.zero.account.domain.AccountUser;
import com.zero.account.dto.AccountDto;
import com.zero.account.exception.AccountException;
import com.zero.account.repository.AccountRepository;
import com.zero.account.repository.AccountUserRepository;
import com.zero.account.type.AccountStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.zero.account.type.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountService accountService;

    @DisplayName("계좌 생성 성공")
    @Test
    void successCreateAccount() {
        // given
        given(accountUserRepository.findById(any())).willReturn(Optional.of(
                AccountUser.builder()
                        .id(1L)
                        .build()
        ));
        given(accountRepository.countByAccountUserId(any())).willReturn(Optional.of(0L));
        given(accountRepository.save(any())).willReturn(
                Account.builder()
                        .id(1L)
                        .accountUser(AccountUser.builder().id(1L).build())
                        .accountNumber("1234")
                        .balance(1000L)
                        .accountStatus(AccountStatus.IN_USE)
                        .registeredAt(LocalDateTime.now())
                        .build()
        );

        // when
        AccountDto account = accountService.createAccount(1L, 1000L);

        // then
        assertThat(account.getUserId()).isEqualTo(1L);
        assertThat(account.getBalance()).isEqualTo(1000L);
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.IN_USE);

    }

    @DisplayName("계좌 생성 실패 - 사용자 없음")
    @Test()
    void failedAccountUserIdCheck() {
        // given


        // when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 100L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(USER_NOT_FOUND.getDescription());
    }

    @DisplayName("계좌 생성 - 사용자 검사 성공")
    @Test
    void successAccountUserIdCheck() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .name("테스트")
                        .build()));

        // when
        AccountUser accountUser =
                accountUserRepository.findById(1L).orElseThrow(() ->
                        new IllegalArgumentException(USER_NOT_FOUND.getDescription()));

        // then
        assertThat(accountUser.getId()).isEqualTo(1L);
        assertThat(accountUser.getName()).isEqualTo("테스트");
    }


    @DisplayName("계좌 생성 실패 - 최대 보유 계좌 초과(10개)")
    @Test
    void failedCreateAccountCheckMaxAccount() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .name("테스트")
                        .build()));
        given(accountRepository.countByAccountUserId(anyLong()))
                .willReturn(Optional.of(10L));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.createAccount(1L, 100L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(TO_MANY_ACCOUNT.getDescription());

    }

    @DisplayName("계좌 생성 - 최대 보유 계좌 미만")
    @Test
    void successCreateAccountCheckMaxAccount() {
        // given
        given(accountRepository.countByAccountUserId(anyLong()))
                .willReturn(Optional.of(0L));

        // when
        long accountCount = accountRepository.countByAccountUserId(1L).get();


        // then
        assertThat(accountCount).isEqualTo(0L);
    }

    @DisplayName("계좌 생성 실패 - 계좌 번호 중복 검사")
    @Test
    void failedAccountCheck() {
        // given
        given(accountRepository.existsByAccountNumber(anyString()))
                .willReturn(true);

        // when
        long num = 5555555555L;
        boolean check = accountRepository.existsByAccountNumber(String.valueOf(num));

        // then
        assertThat(check).isEqualTo(true);
    }

    @DisplayName("계좌 생성 - 계좌 번호 중복 검사 통과")
    @Test
    void successAccountCheck() {
        // given
        long accountMaxNumber = 9999999999L;
        long accountMinNumber = 1000000000L;

        // when
        long num = (long) (Math.random() * (accountMaxNumber - accountMinNumber + 1)) + accountMinNumber;
        boolean check = accountRepository.existsByAccountNumber(String.valueOf(num));

        // then
        assertThat(check).isEqualTo(false);
    }

    @DisplayName("계좌 해지 실패 - 사용자 없음")
    @Test
    void failedDeleteAccountNotFoundUser() {
        // given

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.updateAccountStatus(1L, "1"));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(USER_NOT_FOUND.getDescription());
    }

    @DisplayName("계좌 해지 실패 - 이미 해지된 계좌 or 없는 계좌")
    @Test
    void failedDeleteAccountDeletedAccount() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .build()));

        accountRepository.save(Account.builder()
                .id(1L)
                .accountNumber("123")
                .build());
        // when

        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.updateAccountStatus(1L, "123"));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(ACCOUNT_NOT_FOUND.getDescription());
    }

    @DisplayName("계좌 해지 실패 - 소유주 불일치")
    @Test
    void failedDeleteAccountUserDiffUser() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(AccountUser.builder()
                                .id(2L)
                                .build())
                        .build()));

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.updateAccountStatus(1L, "123"));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(USER_MIS_MATCH.getDescription());
    }

    @DisplayName("계좌 해지 실패 - 잔액 존재 ")
    @Test
    void failedDeleteAccount() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(AccountUser.builder()
                                .id(1L)
                                .build())
                        .balance(123L)
                        .build()));
        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.updateAccountStatus(1L, "123"));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(EXIST_BALANCE.getDescription());
    }

    @DisplayName("계좌 해지 성공")
    @Test
    void successDeleteAccount() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(AccountUser.builder()
                        .id(1L)
                        .build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(2L)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountUser(AccountUser.builder()
                                .id(1L)
                                .build())
                        .balance(0L)
                        .build()));
        given(accountRepository.save(any())).willReturn(
                Account.builder()
                        .id(2L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountUser(AccountUser.builder()
                                .id(1L)
                                .build())
                        .balance(0L)
                        .build()
        );

        // when
        AccountDto account = accountService.updateAccountStatus(1L, "1");

        // then
        assertThat(account.getUserId()).isEqualTo(2L);
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.UNREGISTERED);
    }


    @DisplayName("계좌 조회 실패 - 사용자 없음")
    @Test
    void failedSelectAccountNotFoundUser() {
        // given

        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.selectAccounts(1L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(USER_NOT_FOUND.getDescription());
    }

    @DisplayName("계좌 조회 실패 - 보유한 계좌 없음")
    @Test
    void failedSelectAccountNothaveAccount() {
        // given
        given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(
                AccountUser.builder()
                        .id(1L)
                        .build()
        ));
        // when
        AccountException exception = Assertions.assertThrows(AccountException.class, () ->
                accountService.selectAccounts(1L));

        // then
        assertThat(exception.getErrorMessage()).isEqualTo(NOT_HAVE_ACCOUNT.getDescription());
    }

    @DisplayName("계좌 조회 성공")
    @Test
    void selectAccount() {
        // given
        given(accountRepository.findByAccountUserId(any()))
                .willReturn(Optional.of(Arrays.asList(
                        Account.builder()
                                .id(1L)
                                .accountNumber("11111")
                                .accountUser(AccountUser.builder()
                                        .id(1L)
                                        .build())
                                .balance(0L)
                                .build(),
                        Account.builder()
                                .id(2L)
                                .accountNumber("22222")
                                .accountUser(AccountUser.builder()
                                        .id(1L)
                                        .build())
                                .balance(0L)
                                .build(),
                        (Account.builder()
                                .id(3L)
                                .accountNumber("33333")
                                .accountUser(AccountUser.builder()
                                        .id(1L)
                                        .build())
                                .balance(0L)
                                .build()
                        ))));

        // when
        List<Account> accounts = accountRepository.findByAccountUserId(1L).get();

        // then
        assertThat(accounts.get(0).getId()).isEqualTo(1L);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("11111");
        assertThat(accounts.get(0).getAccountUser().getId()).isEqualTo(1L);
        assertThat(accounts.get(1).getId()).isEqualTo(2L);
        assertThat(accounts.get(1).getAccountNumber()).isEqualTo("22222");
        assertThat(accounts.get(1).getAccountUser().getId()).isEqualTo(1L);
        assertThat(accounts.get(2).getId()).isEqualTo(3L);
        assertThat(accounts.get(2).getAccountNumber()).isEqualTo("33333");
        assertThat(accounts.get(2).getAccountUser().getId()).isEqualTo(1L);
    }

}