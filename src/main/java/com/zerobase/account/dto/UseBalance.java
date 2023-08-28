package com.zerobase.account.dto;

import com.zerobase.account.aop.AccountLockIdInterface;
import com.zerobase.account.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class UseBalance {
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Request implements AccountLockIdInterface {
    @NotNull
    @Min(1)
    private Long userId;
    @NotBlank
    @Size(min = 9 , max = 10, message = "계좌번호가 타당하지 않습니다.")
    private String accountNumber;
    @NotNull @Min(value = 10, message = "유효하지 않는 잔액입니다.") @Max(value = 1000_000_000,
        message = "유효하지 않는 잔액입니다.")
    private Long amount;
  }

  @Getter @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response{
    private String accountNumber;
    private LocalDateTime transactionAt;
    private TransactionResultType transactionResulte;
    private String transactionId;
    private Long amount;

    public static Response from(TransactionDto transactionDto) {
      return Response.builder()
          .accountNumber(transactionDto.getAccountNumber())
          .amount(transactionDto.getAmount())
          .transactionResulte(transactionDto.getTransactionResultType())
          .transactionAt(transactionDto.getTransactionAt())
          .transactionId(transactionDto.getTransactionId())
          .build();
    }

  }
}
