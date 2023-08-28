package com.zerobase.account.controller;

import com.zerobase.account.domain.Account;
import com.zerobase.account.dto.AccountInfo;
import com.zerobase.account.dto.CreateAccount;
import com.zerobase.account.dto.DeleteAccount;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.service.AccountService;
import com.zerobase.account.type.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.account.type.ErrorCode.INVALID_REQUEST;

@RestController
@RequiredArgsConstructor
public class AccountController {
  private final AccountService accountService;

  @PostMapping("/account")
  public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
    return CreateAccount.Response.from(
        accountService.createdAccount(request.getUserId(),
            request.getInitalBalance())
    );
  }

  @GetMapping("/account/{id}")
  public Account getAccount(@PathVariable("id") Long id) {
    return accountService.getAccount(id);
  }

  @DeleteMapping("/account")
  public DeleteAccount.Response deleteAccount(
      @RequestBody @Valid DeleteAccount.Request request) {
    return DeleteAccount.Response.from(
        accountService.deleteAccount(request.getUserId(), request.getAccountNumber())
    );
  }
  @GetMapping("/account")
  public List<AccountInfo> getAccountByUserId(
      @RequestParam("user_id")
      long userId) throws IllegalAccessException {
    if (userId < 0) {
      throw new AccountException(INVALID_REQUEST ,INVALID_REQUEST.getDescription());
    }
    return accountService.getAccountByUserId(userId)
        .stream().map(accountDto -> AccountInfo.builder()
            .accountNumber(accountDto.getAccountNumber())
            .balance(accountDto.getBalance()).build())
        .collect(Collectors.toList());
  }
}
