package com.zerobase.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  USER_NOT_FOUND("사용자가 없습니다."),
  ACCOUNT_NOT_FOUND("계좌가 없습니다."),
  AMOUNT_EXCEED_BALANCE("거래 금액이 잔액보다 큽니다."),
  USER_ACCOUNT_UN_MATCH("사용자와 계좌의 소유주가 다릅니다."),
  MAX_ACCOUNT_PER_USER_10("계좌의 갯수는 10개를 넘을 수 없다."),
  ACCOUNT_ALREADY_UNREGISIERED("계좌가 이미 해지되었습니다."),
  BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지할 수 없습니다."),
  INTERNAL_SERVEL_ERROR("내부 서버 오류"),
  INVALID_REQUEST("잘못된 요청이 들어왔습니다."),
  ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용중이다.");

  private final String description;

}
