package com.zerobase.account.service;

import com.zerobase.account.aop.AccountLockIdInterface;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
  private final LockService lockService;
  //  어떤 경우에 적용할 것인지
  @Around("@annotation(com.zerobase.account.aop.AccountLock) && args(request)")
  public Object aroundMethod(
      ProceedingJoinPoint pjp,
      AccountLockIdInterface request
  ) throws Throwable {
    //lock 취득 시도
    lockService.getLock(request.getAccountNumber());
    try{
      // before
      return pjp.proceed();
      // after
    }  finally {
      //lock 해제
      lockService.unLock(request.getAccountNumber());
    }
  }
}
