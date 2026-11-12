package com.project.orderservice.enity;

public enum Status {
  /** Order persisted and OrderCreatedEvent published; waiting for InventoryService to answer. */
  PENDING,
  SUCCESSFUL,
  RESERVED,
  RELEASED,
  FAILED
}
