package com.zerobase.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

public class DeleteAccount {
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Request {
    @NotNull
    @Min(value = 1 , message = "최소의 범위를 지키지 못했습니다.")
    private Long userId;
    @NotBlank @Length(min = 9, max = 10,message = "계좌번호가 타당하지 않습니다.")
    private String accountNumber;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long userId;
    private String accountNumber;
    private LocalDateTime unRegisteredAt;

    public static Response from(AccountDto accountDto) {
      return Response.builder()
          .userId(accountDto.getUserId())
          .accountNumber(accountDto.getAccountNumber())
          .unRegisteredAt(accountDto.getUnRegisteredAt())
          .build();
    }
  }

}
