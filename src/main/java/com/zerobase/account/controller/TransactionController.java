package com.zerobase.account.controller;

import com.zerobase.account.aop.AccountLock;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@RestController @Slf4j
@RequiredArgsConstructor
public class TransactionController {
  private final TransactionService transactionService;

  @PostMapping("/tansaction/use")
  @AccountLock
  public UseBalance.Response useBalance(
      @Valid @RequestBody UseBalance.Request req
  ) {
    try {
      Thread.sleep(3000L);
      return UseBalance.Response.from(transactionService.useBalance(req.getUserId(),
          req.getAccountNumber(), req.getAmount()));
    } catch (AccountException e) {
      log.error("Failed to use balance.");
      transactionService.saveFailedUseTransaction(
          req.getAccountNumber(),
          req.getAmount()
      );
      throw e;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
