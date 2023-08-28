package com.zerobase.account.service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.domain.Transaction;
import com.zerobase.account.dto.TransactionDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.repository.TransactionRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;
import com.zerobase.account.type.TransactionResultType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.zerobase.account.type.TransactionResultType.*;
import static com.zerobase.account.type.TransactionType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
  private final TransactionRepository transactionRepository;
  private final AccountUserRepository accountUserRepository;
  private final AccountRepository accountRepository;
  /**
   * 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른경우,
   * 계좌가 이미 해지 상태인 경우, 거래 금액이 잔액보다 큰 경우,
   * 거래금액이 너무 작거나 큰 경우 실패 응답*/
  @Transactional
  public TransactionDto useBalance(Long userId, String accountNumber,
                                   Long amount) {
    AccountUser user = accountUserRepository.findById(userId)
        .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

    validateUseBalance(user, account, amount);

    account.useBalance(amount);

    return TransactionDto.fromEntity(saveEndTransaction(S, account, amount));
  }

  private void validateUseBalance(AccountUser user, Account account, Long amount) {
    if (!Objects.equals(user.getAccountUserId(),
        account.getAccountUser().getAccountUserId())) {
      throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
    }
    if (account.getAccountStatus() != AccountStatus.IN_USE) {
      throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISIERED);
    }
    if (account.getBalance() < amount) {
      throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
    }

  }

  @Transactional
  public void saveFailedUseTransaction(String accountNumber, Long amount) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

    saveEndTransaction(F, account, amount);
  }

  private Transaction saveEndTransaction(
      TransactionResultType f, Account account, Long amount) {
    return transactionRepository.save(
        Transaction.builder()
            .transactionType(USE)
            .transactionResultType(f)
            .account(account)
            .amount(amount)
            .balanceSnapshot(account.getBalance())
            .transactionUUID(UUID.randomUUID().toString().replace("-", ""))
            .transactionAt(LocalDateTime.now())
            .build());
  }
}
