package com.zerobase.account.service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.domain.Transaction;
import com.zerobase.account.dto.TransactionDto;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.repository.TransactionRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.TransactionResultType;
import com.zerobase.account.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.account.type.AccountStatus.*;
import static com.zerobase.account.type.TransactionResultType.*;
import static com.zerobase.account.type.TransactionType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private AccountRepository accountRepository;
  @Mock
  private AccountUserRepository accountUserRepository;

  @InjectMocks
  private TransactionService transactionService;

  @Test
  void useBalance() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    Account account = Account.builder()
        .accountUser(pobi)
        .accountStatus(IN_USE)
        .balance(10000L)
        .accountNumber("1000000012")
        .build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(
            account
        ));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .transactionType(USE)
            .transactionResultType(S)
            .transactionUUID("transactionId")
            .transactionAt(LocalDateTime.now())
            .amount(1000L)
            .balanceSnapshot(9000L).build());

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    //when
    TransactionDto transactionDto = transactionService
        .useBalance(1L, "1000000000", 1000L);

    //then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(9000L, transactionDto.getBalanceSnapshot());
    assertEquals(1000L, captor.getValue().getAmount());
    assertEquals(9000L, captor.getValue().getBalanceSnapshot());
    assertEquals(S, transactionDto.getTransactionResultType());
    assertEquals(USE, transactionDto.getTransactionType());
    assertEquals(1000L, transactionDto.getAmount());
  }

  @Test
  void saveFailedUseTransaction() {
  }
}