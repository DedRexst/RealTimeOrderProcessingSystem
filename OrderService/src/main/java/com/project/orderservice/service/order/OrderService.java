package com.project.orderservice.service.order;

import com.project.orderservice.dto.ExternalStatusUpdateDto;
import com.project.orderservice.dto.UpdateOrderStatusDto;
import com.project.orderservice.enity.Order;
import com.project.orderservice.enity.Status;
import com.project.orderservice.service.OrderStatusChange;
import com.project.orderservice.service.OrderStatusUpdateResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  Order createOrder(Order order, @NotNull Long customerId, @NotEmpty @Size List<Long> productIds);

  Order getOrder(Long orderId);

  /**
   * A page of orders, newest first, optionally narrowed to one status or one customer.
   *
   * <p>Read-only and unfiltered by caller: every authenticated caller sees every order. That is the
   * same reach {@code GET /orders/{id}} already has — it never checked whose order it was either —
   * and the two should not disagree. Narrowing both to the caller is a separate change, and needs a
   * link between a token's subject and a customer row that this service does not yet have.
   */
  Map<Long, Order> loadWithDetailsById(Page<Order> page);

  Page<Order> findOrders(Status status, Long customerId, Pageable pageable);

  /**
   * Advances an order to {@link Status#RESERVED} once
   * InventoryService confirms the reservation. Idempotent: replaying the same event is a no-op.
   *
   * @return the transition to announce, or empty when none happened. Returning it rather than
   *     publishing here lets the caller send only after this method's transaction has committed, so
   *     a customer is never emailed about a status that was rolled back — and a duplicate event
   *     produces no second email.
   */
  Optional<OrderStatusChange> markInventoryReserved(Long orderId);

  /**
   * Moves an order at the request of an operator or an external tracking service, rather than in
   * response to the saga. Only transitions the order's lifecycle allows are accepted; asking for
   * the status the order already has is a no-op rather than an error, so a caller that retries a
   * request whose response it never saw does not email the customer twice.
   *
   * @return the order as it now stands, plus the transition to announce once this method's
   * transaction has committed — empty when nothing changed
   * @throws jakarta.persistence.EntityNotFoundException                    if no such order exists
   * @throws com.project.orderservice.util.InvalidStatusTransitionException if the order cannot move
   *                                                                        from its current status to the requested one
   */
  OrderStatusUpdateResult updateOrderStatus(Long orderId, @Valid UpdateOrderStatusDto req);

  /**
   * Moves an order because a third-party system that handles part of its fulfilment says it has
   * moved — a carrier marking it delivered, a payment provider a charge that failed. Validated
   * against the same lifecycle as an operator's request and no-op on the status the order already
   * has, so a callback the reporter retries because it never saw our answer changes nothing and
   * emails no one a second time.
   *
   * <p>What differs from {@link #updateOrderStatus} is attribution and traceability: the reporter
   * is named by the caller rather than by the request body, and the record the report came from
   * travels with the transition to the customer email and the analytics webhook.
   *
   * @param reportedBy the calling system, taken from its token — never from its payload
   * @return the order as it now stands, plus the transition to announce once this method's
   * transaction has committed — empty when nothing changed
   * @throws jakarta.persistence.EntityNotFoundException                    if no such order exists
   * @throws com.project.orderservice.util.InvalidStatusTransitionException if the order cannot move
   *                                                                        from its current status to the reported one
   */
  OrderStatusUpdateResult applyExternalStatusUpdate(
      Long orderId, @Valid ExternalStatusUpdateDto req, String reportedBy);
}
