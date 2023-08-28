package com.zerobase.account.dto;

import com.zerobase.account.type.TransactionResultType;
import com.zerobase.account.type.TransactionType;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {
  private String accountNumber;                       //계좌번호
  private TransactionType transactionType;
  private TransactionResultType transactionResult;    //잔액사용 결과(성공,실패)
  private String transactionId;                       //잔액 사용 ID
  private Long amount;                                //거래 금액
  private LocalDateTime transactedAt;                 //잔액 사용 일시

  public static QueryTransactionResponse from(TransactionDto transactionDto) {
    return QueryTransactionResponse.builder()
        .accountNumber(transactionDto.getAccountNumber())
        .transactionType(transactionDto.getTransactionType())
        .transactionResult(transactionDto.getTransactionResultType())
        .transactionId(transactionDto.getTransactionId())
        .amount(transactionDto.getAmount())
        .transactedAt(transactionDto.getTransactionAt())
        .build();
  }
}
