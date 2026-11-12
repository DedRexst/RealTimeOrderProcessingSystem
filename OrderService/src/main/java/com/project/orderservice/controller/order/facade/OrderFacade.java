package com.project.orderservice.controller.order.facade;

import com.project.orderservice.controller.order.OrderController;
import com.project.orderservice.dto.CreateOrderDto;
import com.project.orderservice.dto.ExternalStatusUpdateDto;
import com.project.orderservice.dto.OrderResponseDto;
import com.project.orderservice.dto.OrderSummaryDto;
import com.project.orderservice.dto.PageResponseDto;
import com.project.orderservice.dto.UpdateOrderStatusDto;
import com.project.orderservice.enity.Status;
import com.project.orderservice.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * What the HTTP layer talks to: one call per endpoint, taking and returning only what the endpoint
 * itself has. Orchestration that used to sit in the controller — attributing a status change to the
 * caller's token, announcing a transition once its transaction has committed — lives behind here,
 * which leaves {@link OrderController} with routing, validation
 * and status codes, and keeps {@link OrderService} free of the web
 * layer's {@link Jwt}.
 */
public interface OrderFacade {

  OrderResponseDto createOrder(@Valid CreateOrderDto req);

  OrderResponseDto getOrder(Long orderId);

  /** A page of orders for the operator console, newest first. See {@code OrderService#listOrders}. */
  PageResponseDto<OrderSummaryDto> listOrders(Status status, Long customerId, Pageable pageable);

  /**
   * Applies an operator's status change and announces it. The caller's token names whoever made the
   * change when the request did not, and the transition reaches the customer email and the
   * analytics webhook only after the service's transaction has committed.
   *
   * @return the order as it now stands, whether or not the request changed anything
   * @throws jakarta.persistence.EntityNotFoundException if no such order exists
   * @throws com.project.orderservice.util.InvalidStatusTransitionException if the order cannot move
   *     from its current status to the requested one
   */
  OrderResponseDto updateOrderStatus(Long orderId, @Valid UpdateOrderStatusDto req, Jwt jwt);

  /**
   * Applies a status a third-party system reports about an order it handles for us, and announces
   * it the same way an operator's change is announced.
   *
   * <p>The reporting system is named by its own token — {@link ExternalStatusUpdateDto} has no
   * field for it — so an integration whose credentials leak can still only ever sign transitions
   * as itself.
   *
   * @return the order as it now stands, whether or not the report changed anything
   * @throws jakarta.persistence.EntityNotFoundException if no such order exists
   * @throws com.project.orderservice.util.InvalidStatusTransitionException if the order cannot move
   *     from its current status to the reported one
   */
  OrderResponseDto applyExternalStatusUpdate(Long orderId, @Valid ExternalStatusUpdateDto req,
      Jwt jwt);
}
