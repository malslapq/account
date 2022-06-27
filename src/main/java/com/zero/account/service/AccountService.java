package com.zero.account.service;

import com.zero.account.domain.Account;
import com.zero.account.domain.AccountUser;
import com.zero.account.dto.AccountDto;
import com.zero.account.exception.AccountException;
import com.zero.account.repository.AccountRepository;
import com.zero.account.repository.AccountUserRepository;
import com.zero.account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.zero.account.type.AccountStatus.IN_USE;
import static com.zero.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        long createAccountNumber;
        AccountUser accountUser = getAccountUser(userId);
        Long accountCnt = accountRepository.countByAccountUserId(accountUser.getId())
                .orElse(0L);

        extracted(accountCnt);

        while (true) {
            Long accountMaxNumber = 9999999999L;
            Long accountMinNumber = 1000000000L;
            createAccountNumber =
                    (long) Math.floor(Math.random() * (accountMaxNumber - accountMinNumber)) + 1000000000L;
            if (accountRepository.existsByAccountNumber(String.valueOf(createAccountNumber))) {
                continue;
            }
            break;
        }

        return AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber(String.valueOf(createAccountNumber))
                        .balance(initialBalance)
                        .accountStatus(IN_USE)
                        .registeredAt(LocalDateTime.now())
                        .build()));
    }

    private void extracted(Long accountCnt) {
        if (accountCnt >= 10L) {
            throw new AccountException(TO_MANY_ACCOUNT);
        }
    }

    @Transactional
    public AccountDto updateAccountStatus(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_MIS_MATCH);
        }
        if (account.getBalance() != 0) {
            throw new AccountException(EXIST_BALANCE);
        }
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());
        return AccountDto.fromEntity(accountRepository.save(account));
    }

    @Transactional
    public List<AccountDto> selectAccounts(Long userId) {
        AccountUser accountUser = getAccountUser(userId);
        List<Account> accounts = accountRepository.findByAccountUserId(accountUser.getId()).orElseThrow(() ->
                new AccountException(NOT_HAVE_ACCOUNT));

        return accounts.stream().map(AccountDto::fromEntity).collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId).orElseThrow(() ->
                new AccountException(USER_NOT_FOUND));
    }

}
