package com.project.inventoryservice.util;

public class NotFoundException extends BusinessException {
  public NotFoundException(ErrorCode errorCode, Object... args) {
    super(errorCode, args);
  }
}
