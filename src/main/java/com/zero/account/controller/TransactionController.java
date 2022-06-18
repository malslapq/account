package com.zero.account.controller;

import com.zero.account.aop.AccountLock;
import com.zero.account.dto.TransactionCancel;
import com.zero.account.dto.TransactionInfo;
import com.zero.account.dto.TransactionUse;
import com.zero.account.exception.AccountException;
import com.zero.account.service.TransactionService;
import com.zero.account.type.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transaction/use.do")
    @AccountLock
    public TransactionUse.Response transactionUse(
            @RequestBody @Valid TransactionUse.Request request) throws InterruptedException {
        try {
            Thread.sleep(3000L);
            return TransactionUse.Response.from(
                    transactionService.
                            transactionUse(
                                    request.getUserId(),
                                    request.getAccountNumber(),
                                    request.getTransactionAmount()));
        } catch (AccountException e) {
            log.error("Failed TransactionUse");
            transactionService.createFailedTransaction(
                    request.getAccountNumber(),
                    request.getTransactionAmount(),
                    TransactionStatus.APPROVAL);
            throw e;
        }

    }

    @PostMapping("/transaction/cancel.do")
    @AccountLock
    public TransactionCancel.Response transactionCancel(
            @RequestBody @Valid TransactionCancel.Request request) throws InterruptedException {
        try {
            Thread.sleep(3000L);
            return TransactionCancel.Response.from(
                    transactionService.transactionCancel(
                            request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getTransactionAmount()),
                    request.getAccountNumber());
        } catch (AccountException e) {
            log.error("Failed TransactionCancel");
            transactionService.createFailedTransaction(
                    request.getAccountNumber(),
                    request.getTransactionAmount(),
                    TransactionStatus.CANCEL);
            throw e;
        }

    }

    @GetMapping("/transactions/{transactionId}")
    public TransactionInfo selectTransactions(@PathVariable @NotNull Long transactionId) {
        return transactionService.selectTransaction(transactionId);
    }


}
