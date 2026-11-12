package com.project.orderservice.util;

import com.project.orderservice.enity.Status;

/**
 * A status change that the order's lifecycle does not allow — reviving a FAILED order, or confirming
 * one whose stock was never reserved. A conflict with the order's current state rather than a
 * malformed request, which is what makes it a 409 and not a 400.
 */
public class InvalidStatusTransitionException extends RuntimeException {

  public InvalidStatusTransitionException(Long orderId, Status current, Status requested) {
    super("Order %s cannot move from %s to %s".formatted(orderId, current, requested));
  }
}
