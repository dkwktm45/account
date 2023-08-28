package com.zerobase.account.controller;

import com.zerobase.account.aop.AccountLock;
import com.zerobase.account.dto.CancelBalance;
import com.zerobase.account.dto.QueryTransactionResponse;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@RestController @Slf4j
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
  private final TransactionService transactionService;

  @PostMapping("/use")
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
  @PostMapping("/cancel")
  @AccountLock
  public CancelBalance.Response cancelBalance(
      @Valid @RequestBody CancelBalance.Request req
  ) {
    try {
      return CancelBalance.Response.from(
          transactionService.cancelBalance(req.getTransactionId(),
              req.getAccountNumber(), req.getAmount())
      );
    } catch (AccountException e) {
      log.error("Failed to use balance. ");

      //실패건 저장.
      transactionService.saveFailedUseTransaction(
          req.getAccountNumber(),
          req.getAmount()
      );
      throw e;
    }
  }

  //잔액사용 확인(거래조회)
  @GetMapping("/{transactionId}")
  public QueryTransactionResponse queryTransaction(
      @PathVariable String transactionId) {
    return QueryTransactionResponse.from(
        transactionService.queryTransaction(transactionId)
    );
  }
}
