package com.project.authservice.util.exception;

import lombok.Getter;

/** The single exception for every expected business failure; see {@link ErrorCode}. */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode, Object... args) {
    super(errorCode.getMessageTemplate().formatted(args));
    this.errorCode = errorCode;
  }
}
