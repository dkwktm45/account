package com.zerobase.account.exception;

import com.zerobase.account.dto.ErrorResponse;
import com.zerobase.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.zerobase.account.type.ErrorCode.INVALID_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccountException.class)
  public ErrorResponse handleAccountException(AccountException e) {
    log.error("{} is occurred : ", e.getMessage());
    return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ErrorResponse notVaildException(MethodArgumentNotValidException e) {
    log.error("{} is occurred : ", e.getMessage());
    return new ErrorResponse(
        INVALID_REQUEST,
        e.getFieldError().getDefaultMessage());
  }
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ErrorResponse handleDataIntegerException(DataIntegrityViolationException e) {
    log.error("DataIntegrityViolationException is occurred : ");
    return new ErrorResponse(
        ErrorCode.INVALID_REQUEST,
        ErrorCode.INVALID_REQUEST.getDescription());
  }
}
