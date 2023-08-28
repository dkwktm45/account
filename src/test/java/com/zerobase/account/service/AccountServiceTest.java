package com.zerobase.account.service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 컨트롤러를 테스트하는 2가지 대표적인 방법
 * - @SpringBootTest + @AutoConfigureMockMvc -> 전체 Bean을 모두 생성한 후 mockMvc를 통해 http요청과 검증을 진행
 * - @WebMvcTest -> 내가 필요로 하는 MVC관련 Bean들만 생성
 * - Controller, ControllerAdvic , Converter , Filter, HandlerInteceptor 등
 * - Service 등을 목킹해서 사용하는 방식
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountUserRepository accountUserRepository;
  @InjectMocks
  private AccountService accountService;

  @Test
  @DisplayName("계좌 조회 성공")
  void createAccountSuccess() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findFirstByOrderByAccountIdDesc())
        .willReturn(Optional.of(Account.builder()
            .accountUser(pobi)
            .accountNumber("1234").build()));
    given(accountRepository.save(any()))
        .willReturn(Account.builder()
            .accountUser(pobi)
            .accountNumber("1234").build());

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    //when
    AccountDto accountDto = accountService.createdAccount(1L, 200L);
    //then
    verify(accountRepository, times(1)).save(captor.capture());
    assertEquals(12L, accountDto.getUserId());
    assertEquals("1234", accountDto.getAccountNumber());
  }

  @Test
  @DisplayName("해당 유저 없음 - 계좌 생성 실패")
  void createAccount_UserNotFound() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.createdAccount(1L, 100L));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌가 10개 이상인 경우 - 계좌 생성 실패")
  void createAccount_Max10() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.countByAccountUser(any()))
        .willReturn(10);
    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.createdAccount(1L, 100L));

    //then
    assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 해지 성공")
  void deleteAccountSuccess() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(pobi).balance(0L).accountNumber("12345").build()));

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    //when
    AccountDto accountDto = accountService.deleteAccount(1L, "12345");
    //then
    verify(accountRepository, times(1)).save(captor.capture());
    assertEquals(12L, accountDto.getUserId());
    assertEquals("12345", accountDto.getAccountNumber());
  }

  @Test
  @DisplayName("해당 유저 없음 - 계좌 해지 실패")
  void deleteAccountFail() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L, "12345"));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
  void deleteAccount_accountNotFOUND() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.empty());
    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L, "12345"));

    //then
    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 소유주 다름 - 계좌 해지 실패")
  void deleteAccount_userUnMatch() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    AccountUser pobi2 = AccountUser.builder()
        .accountUserId(13L).name("Pobi").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(
            Account.builder()
                .accountUser(pobi2)
                .accountNumber("1234").build()));
    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L, "12345"));

    //then
    assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
  }

  @Test
  @DisplayName("잔액이 남음 - 계좌 해지 실패")
  void deleteAccount_balanceNotEmpty() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(
            Account.builder()
                .accountUser(pobi)
                .balance(100L)
                .accountNumber("1234").build()));
    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L, "12345"));

    //then
    assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
  }
  @Test
  @DisplayName("계좌가 이미 해지인 경우 - 계좌 해지 실패")
  void deleteAccount_alreadyUnregistered() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(
            Account.builder()
                .accountUser(pobi)
                .accountStatus(AccountStatus.UNREGISTERED)
                .balance(0l)
                .accountNumber("1234").build()));
    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.deleteAccount(1L, "12345"));

    //then
    assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISIERED, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 여러개 성공")
  void getAccountByUserIdSuccess() {
    //given
    AccountUser pobi = AccountUser.builder()
        .accountUserId(12L).name("Pobi").build();
    //given
    List<Account> accountList = Arrays.asList(
        Account.builder()
            .accountId(1L)
            .accountUser(pobi)
            .accountNumber("123456")
            .balance(1000L).build(),
        Account.builder()
            .accountId(2L)
            .accountUser(pobi)
            .accountNumber("1234566")
            .balance(2000L).build());

    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(pobi));
    given(accountRepository.findByAccountUser(any()))
        .willReturn(accountList);

    //when
    List<AccountDto> accountDto = accountService.getAccountByUserId(1L);
    //then
    assertEquals(2, accountDto.size());
    assertEquals("123456", accountDto.get(0).getAccountNumber());
    assertEquals(1000L, accountDto.get(0).getBalance());
    assertEquals("1234566", accountDto.get(1).getAccountNumber());
    assertEquals(2000L, accountDto.get(1).getBalance());
  }
  @Test
  @DisplayName("계좌 여러개 실패")
  void getAccountByUserIdFail() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.getAccountByUserId(1L));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

}