package com.project.inventoryservice.util;

public enum ErrorCode {
  BSC1("Product with id %s not found"),
  BSC2("Product with id %s has insufficient stock: requested %s, available %s"),
  BSC3("Order with id %s contains no products");

  private final String message;

  ErrorCode(final String message) {
    this.message = message;
  }

  public String getMessage(final Object... args) {
    return this.message.formatted(args);
  }

  public String getFullMessage(final Object... args) {
    return this.name() + " - " + this.getMessage(args);
  }
}
