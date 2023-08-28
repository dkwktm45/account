package com.zerobase.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.dto.CreateAccount;
import com.zerobase.account.dto.DeleteAccount;
import com.zerobase.account.service.AccountService;
import com.zerobase.account.service.LockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.zerobase.account.type.ErrorCode.INVALID_REQUEST;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
  @MockBean
  private AccountService accountService;
  @MockBean
  private LockService redisTestService;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void CreateAccount_success() throws Exception {
    //given
    given(accountService.createdAccount(anyLong(), anyLong()))
        .willReturn(AccountDto.builder()
            .userId(1L)
            .accountNumber("12345")
            .registeredAt(LocalDateTime.now())
            .unRegisteredAt(LocalDateTime.now())
            .build());
    //when
    mockMvc.perform(post("/account")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CreateAccount.Request(1L, 100L)
            )))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.accountNumber").value("12345"))
        .andDo(print());
  }

  @Test
  void CreateAccount_fail() throws Exception {
    //given
    CreateAccount.Request req = new CreateAccount.Request(1L, -1L);
    //when
    mockMvc.perform(post("/account")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("타당하지 않은 잔액입니다."))
        .andDo(print());
  }

  @Test
  void DeleteAccount_success() throws Exception {
    //given
    given(accountService.deleteAccount(anyLong(), anyString()))
        .willReturn(AccountDto.builder()
            .userId(1L)
            .accountNumber("100000000")
            .registeredAt(LocalDateTime.now())
            .unRegisteredAt(LocalDateTime.now())
            .build());
    //then
    mockMvc.perform(delete("/account")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new DeleteAccount.Request(1L, "100000000")
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.accountNumber").value("100000000"))
        .andDo(print());
  }
  @Test
  void DeleteAccount_fail() throws Exception {
    DeleteAccount.Request req = new DeleteAccount.Request(1L, "12345");
    //then
    mockMvc.perform(delete("/account")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("계좌번호가 타당하지 않습니다."))
        .andDo(print());
  }
  @Test
  void GetAccount_success() throws Exception {
    //given
    List<AccountDto> accountDtos = Arrays.asList(
        AccountDto.builder()
            .accountNumber("123456")
            .balance(1000L).build(),
        AccountDto.builder()
            .accountNumber("123456")
            .balance(1000L).build());

    given(accountService.getAccountByUserId(anyLong()))
        .willReturn(accountDtos);
    //then
    mockMvc.perform(get("/account?user_id=1")
            .contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].balance").value(1000L))
        .andExpect(jsonPath("$[0].accountNumber").value("123456"))
        .andDo(print());
  }

  @Test
  void GetAccount_fail() throws Exception {
    String id = "-1";
    //then
    mockMvc.perform(get("/account?user_id=" + id)
            .contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청이 들어왔습니다."))
        .andDo(print());
  }
}