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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.account.type.ErrorCode.ACCOUNT_ALREADY_UNREGISTERED;
import static com.zerobase.account.type.ErrorCode.TRANSACTION_NOT_FOUND;
import static com.zerobase.account.type.TransactionResultType.F;
import static com.zerobase.account.type.TransactionResultType.S;
import static com.zerobase.account.type.TransactionType.CANCEL;
import static com.zerobase.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
  public static final long AMOUNT_USE = 200L;
  public static final long AMOUNT_CANCEL = 200L;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountUserRepository accountUserRepository;

  @InjectMocks
  private TransactionService transactionService;

  @Test
  void successUseBalance() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(1L).name("Pobi").build();
    Account account = Account.builder()
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(user));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionUUID("transactionId")
            .transactionAt(LocalDateTime.now())
            .amount(1000L)
            .balanceSnapshot(9000L)
            .build());
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    // when
    TransactionDto transactionDto = transactionService.useBalance(1L,
        "1000000000",
        AMOUNT_USE);

    // then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(AMOUNT_USE, captor.getValue().getAmount());
    assertEquals(9800L, captor.getValue().getBalanceSnapshot());
    assertEquals(S, transactionDto.getTransactionResultType());
    assertEquals(USE, transactionDto.getTransactionType());
    assertEquals(9000L, transactionDto.getBalanceSnapshot());
    assertEquals(1000L, transactionDto.getAmount());
  }

  @Test
  void useBalance_UserNotFound() {
    // given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());
    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useBalance(1L, "1000000000", 1000L));

    // then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void deleteAccount_AccountNotFound() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(1L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(user));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.empty());

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useBalance(1L, "1000000000", 1000L));

    // then
    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void deleteAccountFailed_userUnMatch() {
    // given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(1L).name("pobi").build();
    AccountUser Pikachu = AccountUser.builder()
        .accountUserId(2L).name("Pikachu").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(Pikachu)
            .balance(0L)
            .accountNumber("1000000012").build()));
    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useBalance(1L, "1000000000", 1000L));

    // then
    assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
  }

  @Test
  void deleteAccountFailed_alreadyUnregistered() {
    // given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(1L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(pobi)
            .accountStatus(AccountStatus.UNREGISTERED) //이미 계좌해지됨
            .balance(0L)
            .accountNumber("1000000012").build()));
    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useBalance(1L, "1000000000", 1000L));

    // then
    assertEquals(ACCOUNT_ALREADY_UNREGISTERED.getDescription(),
        exception.getErrorCode().getDescription());
  }

  @Test
  void exceedAmount_UseBalance() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(100L)
        .accountNumber("1000000012").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(user));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));

    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useBalance(1L, "1000000000", 1000L));
    assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    // then
    verify(transactionRepository, times(0)).save(any());
  }

  @Test
  void saveFailedUseTransaction() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionUUID("transactionId")
            .transactionAt(LocalDateTime.now())
            .amount(1000L)
            .balanceSnapshot(9000L)
            .build());

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    // when
    transactionService.saveFailedUseTransaction("1000000000", AMOUNT_USE);

    // then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(AMOUNT_USE, captor.getValue().getAmount());
    assertEquals(10000L, captor.getValue().getBalanceSnapshot());
    assertEquals(F, captor.getValue().getTransactionResultType());
  }

  @Test
  void successCancelBalance() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(USE)
        .transactionResultType(S)
        .transactionUUID("transactionId")
        .transactionAt(LocalDateTime.now())
        .amount(AMOUNT_CANCEL)
        .balanceSnapshot(9000L)
        .build();
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .transactionType(CANCEL)
            .transactionResultType(S)
            .transactionUUID("transactionIdForCancel")
            .transactionAt(LocalDateTime.now())
            .amount(AMOUNT_CANCEL)
            .balanceSnapshot(10000L)
            .build());
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    // when
    TransactionDto transactionDto = transactionService.cancelBalance("transactionId",
        "1000000000", AMOUNT_CANCEL);

    // then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(AMOUNT_CANCEL, captor.getValue().getAmount());
    assertEquals(10000L + AMOUNT_CANCEL, captor.getValue().getBalanceSnapshot());
    assertEquals(S, transactionDto.getTransactionResultType());
    assertEquals(CANCEL, transactionDto.getTransactionType());
    assertEquals(10000L, transactionDto.getBalanceSnapshot());
    assertEquals(AMOUNT_CANCEL, transactionDto.getAmount());
  }

  @Test
  void cancelTransaction_Account_AccountNotFound() {
    // given
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(Transaction.builder()
            .transactionType(USE)
            .transactionResultType(S)
            .transactionUUID("transactionId")
            .transactionAt(LocalDateTime.now())
            .amount(AMOUNT_CANCEL)
            .balanceSnapshot(9000L)
            .build()));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.empty());

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

    // then
    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void cancelTransaction_Account_TransactionNotFound() {
    // given
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.empty());

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

    // then
    assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void cancelTransaction_TransactionAccountUnMatch() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountId(1L)
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    Account accountNotUse = Account.builder()
        .accountUser(user).accountId(2L)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000013").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(USE)
        .transactionResultType(S)
        .transactionUUID("transactionId")
        .transactionAt(LocalDateTime.now())
        .amount(AMOUNT_CANCEL)
        .balanceSnapshot(9000L)
        .build();
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(accountNotUse));

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.cancelBalance("transactionId", "1000000000", AMOUNT_CANCEL));
    // then
    assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
  }

  @Test
  void cancelTransaction_CancelMustFully() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(1L).name("Pobi").build();
    Account account = Account.builder()
        .accountId(2L).accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(USE)
        .transactionResultType(S)
        .transactionUUID("transactionId")
        .transactionAt(LocalDateTime.now())
        .amount(AMOUNT_CANCEL + 1000L)
        .balanceSnapshot(9000L)
        .build();
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService
            .cancelBalance(
                "transactionId", "1000000000", AMOUNT_CANCEL
            )
    );

    // then
    assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
  }

  @Test
  void cancelTransaction_TooOldOrder() {
    // given
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountId(1L)
        .accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(USE)
        .transactionResultType(S)
        .transactionUUID("transactionId")
        .transactionAt(LocalDateTime.now().minusYears(1).minusDays(1))
        .amount(AMOUNT_CANCEL)
        .balanceSnapshot(9000L)
        .build();
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService
            .cancelBalance(
                "transactionId", "1000000000", AMOUNT_CANCEL
            )
    );

    // then
    assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
  }

  @Test
  void successQueryTransaction() {
    //given 
    AccountUser user = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountId(1L).accountUser(user)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(USE)
        .transactionResultType(S)
        .transactionUUID("transactionId")
        .transactionAt(LocalDateTime.now().minusYears(1).minusDays(1))
        .amount(AMOUNT_CANCEL)
        .balanceSnapshot(9000L)
        .build();
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.of(transaction));
    //when 
    TransactionDto transactionDto = transactionService.queryTransaction("trxId");

    //then
    assertEquals(USE, transactionDto.getTransactionType());
    assertEquals(S, transactionDto.getTransactionResultType());
    assertEquals(AMOUNT_CANCEL, transactionDto.getAmount());
    assertEquals("transactionId", transactionDto.getTransactionId());
  }

  @Test
  void queryTransaction_Account_TransactionNotFound() {
    // given
    given(transactionRepository.findByTransactionUUID(anyString()))
        .willReturn(Optional.empty());

    // when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.queryTransaction("transactionId"));

    // then
    assertEquals(TRANSACTION_NOT_FOUND, exception.getErrorCode());
  }

}