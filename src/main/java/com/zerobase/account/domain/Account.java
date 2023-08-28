package com.zerobase.account.domain;

import com.zerobase.account.exception.AccountException;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account extends BaseEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long accountId;

  private String accountNumber;
  @ManyToOne
  private AccountUser accountUser;
  private Long balance;
  @CreatedDate
  private LocalDateTime registeredAt;

  @LastModifiedDate
  private LocalDateTime unRegisteredAt;

  @Enumerated(EnumType.STRING)
  private AccountStatus accountStatus;

  public void useBalance(Long amount) {
    if (amount > balance) {
      throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
    }
    balance -= amount;
  }
  public void cancelBalance(Long amount) {
    if (amount < 0) {
      throw new AccountException(ErrorCode.INVALID_REQUEST);
    }
    balance +=amount;
  }
}
