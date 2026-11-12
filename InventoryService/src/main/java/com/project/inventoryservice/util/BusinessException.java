package com.project.inventoryservice.util;

public class BusinessException extends RuntimeException {
  private final ErrorCode code;

  public BusinessException(ErrorCode errorCode, Object... args) {
    super(errorCode.getFullMessage(args));
    this.code = errorCode;
  }
}
