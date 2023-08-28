package com.zerobase.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.account.dto.CancelBalance;
import com.zerobase.account.dto.TransactionDto;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.zerobase.account.type.TransactionResultType.S;
import static com.zerobase.account.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @MockBean
  private TransactionService transactionService;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void useBalance_success() throws Exception {
    given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactionId("testId")
            .amount(1000L)
            .transactionResultType(S)
            .transactionAt(LocalDateTime.now()).build());
    //when
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UseBalance.Request(12L, "1000000000", 3000L)
            ))).andDo(print())
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.amount").value(1000L))
        .andExpect(jsonPath("$.transactionId").value("testId"))
        .andExpect(jsonPath("$.transactionResulte").value("S"));
  }

  @Test
  void useBalance_fail_account() throws Exception {
    //given
    UseBalance.Request req = new UseBalance.Request(12L, "111", 3000L);

    //when
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req))).andDo(print())
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("계좌번호가 타당하지 않습니다."))
        .andDo(print());
  }
  @Test
  void useBalance_fail_amount() throws Exception {
    //given
    UseBalance.Request req = new UseBalance.Request(12L, "1000000000", 3L);

    //when
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req))).andDo(print())
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("유효하지 않는 잔액입니다."))
        .andDo(print());
  }
  @Test
  void successCancelBalance() throws Exception {
    //given
    given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactionAt(LocalDateTime.now())
            .amount(54321L)
            .transactionId("transactionIdForCancel")
            .transactionResultType(S)
            .build());
    //when
    mockMvc.perform(post("/transaction/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CancelBalance.Request("transactionId","2000000000", 3000L)
            ))
        ).andDo(print())
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.transactionResult").value("S"))
        .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
        .andExpect(jsonPath("$.amount").value(54321));
  }

  @Test
  void successQueryTransaction() throws Exception {
    //given
    given(transactionService.queryTransaction(anyString()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactionType(USE)
            .transactionAt(LocalDateTime.now())
            .amount(54321L)
            .transactionId("transactionIdForCancel")
            .transactionResultType(S)
            .build());
    //when
    mockMvc.perform(get("/transaction/12345"))
        .andDo(print())
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.transactionType").value("USE"))
        .andExpect(jsonPath("$.transactionResult").value("S"))
        .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
        .andExpect(jsonPath("$.amount").value(54321));
  }
}