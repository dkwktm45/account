package com.zerobase.account.service;

import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.zerobase.account.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
  @Mock
  private LockService lockService;

  @Mock
  private ProceedingJoinPoint proceedingJoinPoint;

  @InjectMocks
  private LockAopAspect lockAopAspect;

  @Test
  void lockAndUnlock() throws Throwable {
    //given
    ArgumentCaptor<String> lockArgCaptor =
        ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> unlockArgCaptor =
        ArgumentCaptor.forClass(String.class);
    UseBalance.Request request =
        new UseBalance.Request(1234L, "1234", 19000L);

    //when
    lockAopAspect.aroundMethod(proceedingJoinPoint, request);
    //then
    verify(lockService, times(1)).getLock(
        lockArgCaptor.capture()
    );
    verify(lockService, times(1)).unLock(
        unlockArgCaptor.capture()
    );
    assertEquals("1234",lockArgCaptor.getValue());
    assertEquals("1234",unlockArgCaptor.getValue());
  }
  @Test
  void lockAndUnlock_evenIfThrow() throws Throwable {
    //given
    ArgumentCaptor<String> lockArgCaptor =
        ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> unlockArgCaptor =
        ArgumentCaptor.forClass(String.class);
    UseBalance.Request request =
        new UseBalance.Request(1234L, "34534", 19000L);
    given(proceedingJoinPoint.proceed())
        .willThrow(new AccountException(ACCOUNT_NOT_FOUND));

    //when
    assertThrows(AccountException.class, () ->
        lockAopAspect.aroundMethod(proceedingJoinPoint, request));
    //then
    verify(lockService, times(1)).getLock(
        lockArgCaptor.capture()
    );
    verify(lockService, times(1)).unLock(
        unlockArgCaptor.capture()
    );
    assertEquals("34534",lockArgCaptor.getValue());
    assertEquals("34534",unlockArgCaptor.getValue());
  }


}