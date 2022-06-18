package com.zero.account.controller;

import com.zero.account.dto.AccountInfo;
import com.zero.account.dto.CreateAccount;
import com.zero.account.dto.PatchAccount;
import com.zero.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        System.out.println(request.getUserId());
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()));
    }

    @PatchMapping("/account")
    public PatchAccount.Response updateAccountStatus(@RequestBody @Valid PatchAccount.Request request) {
        return PatchAccount.Response.from(
                accountService.updateAccountStatus(
                        request.getUserId(),
                        request.getAccountNumber()));
    }

    @GetMapping("/accounts")
    public List<AccountInfo> selectAccounts(
            @RequestParam("userId") @NotBlank(message = "사용자 아이디를 입력해야 합니다.") Long userId) {
        return accountService.selectAccounts(userId).stream().map(accountDto ->
                        AccountInfo.builder()
                                .accountNumber(accountDto.getAccountNumber())
                                .balance(accountDto.getBalance())
                                .build())
                .collect(Collectors.toList());
    }

}
