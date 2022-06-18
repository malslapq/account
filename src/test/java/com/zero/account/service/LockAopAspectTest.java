package com.zero.account.service;

import com.zero.account.dto.TransactionUse;
import com.zero.account.exception.AccountException;
import com.zero.account.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {

    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;

    @DisplayName("lock 테스트")
    @Test
    void lockAndUnlock() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TransactionUse.Request request = new TransactionUse.Request(
                123L, "12345", 1000L);

        // when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);


        // then
        verify(lockService, times(1))
                .lock(lockArgumentCaptor.capture());
        verify(lockService, times(1))
                .unlock(unlockArgumentCaptor.capture());
        assertThat(lockArgumentCaptor.getValue()).isEqualTo("12345");
        assertThat(unlockArgumentCaptor.getValue()).isEqualTo("12345");
    }

    @DisplayName("예외 발생시 lock 테스트")
    @Test
    void lockAndUnlock_evenIfThrow() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TransactionUse.Request request = new TransactionUse.Request(
                123L, "54321", 1000L);
        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        // when
        assertThrows(AccountException.class, () ->
                lockAopAspect.aroundMethod(proceedingJoinPoint, request));


        // then
        verify(lockService, times(1))
                .lock(lockArgumentCaptor.capture());
        verify(lockService, times(1))
                .unlock(unlockArgumentCaptor.capture());
        assertThat(lockArgumentCaptor.getValue()).isEqualTo("54321");
        assertThat(unlockArgumentCaptor.getValue()).isEqualTo("54321");
    }

}