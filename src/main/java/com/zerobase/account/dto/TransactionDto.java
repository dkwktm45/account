package com.zerobase.account.dto;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.Transaction;
import com.zerobase.account.type.TransactionResultType;
import com.zerobase.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
  private String accountNumber;
  private TransactionType transactionType;
  private TransactionResultType transactionResultType;
  private Account account;
  private Long amount;
  private Long balanceSnapshot;

  private String transactionId;
  private LocalDateTime transactionAt;

  public static TransactionDto fromEntity(Transaction transaction) {
    return TransactionDto.builder()
        .accountNumber(transaction.getAccount().getAccountNumber())
        .transactionType(transaction.getTransactionType())
        .transactionId(transaction.getTransactionUUID())
        .account(transaction.getAccount())
        .amount(transaction.getAmount())
        .balanceSnapshot(transaction.getBalanceSnapshot())
        .transactionResultType(transaction.getTransactionResultType())
        .transactionAt(LocalDateTime.now()).build();
  }
}
