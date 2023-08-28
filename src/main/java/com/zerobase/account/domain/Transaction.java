package com.zerobase.account.domain;

import com.zerobase.account.type.TransactionResultType;
import com.zerobase.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Transaction extends BaseEntity{

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transactionId;
  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;
  @Enumerated(EnumType.STRING)
  private TransactionResultType transactionResultType;
  @ManyToOne
  private Account account;
  private Long amount;
  private Long balanceSnapshot;

  private String transactionUUID;
  private LocalDateTime transactionAt;


}
