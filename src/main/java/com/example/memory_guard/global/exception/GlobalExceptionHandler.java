package com.example.memory_guard.global.exception;

import com.example.memory_guard.global.exception.custom.AuthenticationException;
import com.example.memory_guard.global.exception.custom.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("잘못된 인자 값으로 인한 요청 실패: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
    log.warn("잘못된 상태: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse("INVALID_STATE", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.warn("인증 실패: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse("AUTHENTICATION_FAILED", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex) {
    log.warn("잘못된 요청: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse("INVALID_REQUEST", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
    log.warn("IO 처리 실패: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse("FILE_IO_ERROR", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("예외 발생", ex);
    ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}


