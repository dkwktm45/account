package com.zerobase.account.service;

import com.zerobase.account.exception.AccountException;
import com.zerobase.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {

  // cofing에서 설정한 이름과 같아야함
  private final RedissonClient redissonClient;

  public String getLock(String accountNumber) {
    // 락 키
    RLock lock = redissonClient.getLock(getLockKey(accountNumber));
    log.debug("Trying lock for accountNumber : {}", accountNumber);
    try {
      boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
      if (!isLock) {
        log.error("------------------lock acquisition failed--------------");
        throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK,
            ErrorCode.ACCOUNT_TRANSACTION_LOCK.getDescription());
      }
    } catch (AccountException e) {
      throw e;
    } catch (Exception e) {
      log.error("redis lock failed");
    }

    return "get lock success";
  }

  public void unLock(String accountNumber) {
    log.debug("Unlock for accountNumber: {}", accountNumber);
    redissonClient.getLock(getLockKey(accountNumber)).unlock();
  }

  private String getLockKey(String accountNumber) {
    return "ACLK : " + accountNumber;
  }
}
