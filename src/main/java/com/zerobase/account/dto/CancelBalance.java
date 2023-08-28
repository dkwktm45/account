package com.zerobase.account.dto;
import com.zerobase.account.aop.AccountLockIdInterface;
import com.zerobase.account.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class CancelBalance {
  /**
   * {
   * "transactionId":"c2033bb6d82a4250aecf8e27c49b63f6",
   * "accountNumber":"1000000000",
   * "amount":1000
   * }
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request implements AccountLockIdInterface {
    @NotBlank
    private String transactionId;

    @NotNull
    @Size(min = 10, max = 10 , message = "계좌번호가 타당하지 않습니다.")
    private String accountNumber;

    @NotNull
    @Min(value = 10, message = "유효하지 않는 잔액입니다.")
    @Max(value = 1000_000_000, message = "유효하지 않는 잔액입니다.")
    private Long amount;
  }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private String accountNumber;
    private TransactionResultType transactionResult;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;

    public static Response from(TransactionDto transactionDto) {
      return Response.builder()
          .accountNumber(transactionDto.getAccountNumber())
          .transactionResult(transactionDto.getTransactionResultType())
          .transactionId(transactionDto.getTransactionId())
          .amount(transactionDto.getAmount())
          .transactedAt(transactionDto.getTransactionAt())
          .build();
    }
  }
}
