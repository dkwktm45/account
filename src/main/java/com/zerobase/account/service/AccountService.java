package com.zerobase.account.service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.zerobase.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final AccountUserRepository accountUserRepository;

  /**
   * 사용자가 있는지 조회
   * 계좌의 번호를 생성하고
   * 계좌를 저장하고, 그 정보를 넘긴다.
   *
   * @param userId
   * @param intialBalance
   */
  @Transactional
  public AccountDto createdAccount(Long userId, Long intialBalance) {
    AccountUser accountUser = getAccountUser(userId);

    validateCreateAccount(accountUser);

    String newAccountNumber = accountRepository.findFirstByOrderByAccountIdDesc()
        .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
        .orElse("1000000");

    return AccountDto
        .fromEntity(accountRepository.save(
            Account.builder()
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(accountUser)
                .balance(intialBalance)
                .accountNumber(newAccountNumber)
                .build()
        ));
  }

  private AccountUser getAccountUser(Long userId) {
    AccountUser accountUser = accountUserRepository.findById(userId)
        .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    return accountUser;
  }

  private void validateCreateAccount(AccountUser accountUser) {
    if (accountRepository.countByAccountUser(accountUser) >= 10) {
      throw new AccountException(MAX_ACCOUNT_PER_USER_10);
    }
  }

  @Transactional
  public Account getAccount(long id) {
    return accountRepository.findById(id).get();
  }

  @Transactional
  public AccountDto deleteAccount(Long userId, String accountNumber) {
    AccountUser accountUser = getAccountUser(userId);
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

    validateDeleteAccount(accountUser, account);

    account.setAccountStatus(AccountStatus.UNREGISTERED);
    account.setUnRegisteredAt(LocalDateTime.now());
    accountRepository.save(account);

    return AccountDto.fromEntity(account);
  }

  private void validateDeleteAccount(AccountUser accountUser, Account account) {
    if (!Objects.equals(accountUser.getAccountUserId(), account.getAccountUser().getAccountUserId())) {
      throw new AccountException(USER_ACCOUNT_UN_MATCH);
    }
    if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
      throw new AccountException(ACCOUNT_ALREADY_UNREGISIERED);
    }
    if (account.getBalance() > 0) {
      throw new AccountException(BALANCE_NOT_EMPTY);
    }
  }

  public List<AccountDto> getAccountByUserId(long userId) {
    AccountUser accountUser = getAccountUser(userId);

    List<Account> accountList = accountRepository.findByAccountUser(accountUser);

    return accountList.stream()
        .map(AccountDto::fromEntity).collect(Collectors.toList());
  }
}
