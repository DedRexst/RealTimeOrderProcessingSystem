package com.project.orderservice.controller.order.facade.impl;

import com.project.orderservice.controller.order.facade.mapper.OrderMapper;
import com.project.orderservice.dto.CreateOrderDto;
import com.project.orderservice.dto.ExternalStatusUpdateDto;
import com.project.orderservice.dto.OrderResponseDto;
import com.project.orderservice.dto.OrderSummaryDto;
import com.project.orderservice.dto.PageResponseDto;
import com.project.orderservice.dto.UpdateOrderStatusDto;
import com.project.orderservice.enity.Order;
import com.project.orderservice.enity.Status;
import com.project.orderservice.controller.order.facade.OrderFacade;
import com.project.orderservice.producer.OrderStatusChangePublisher;
import com.project.orderservice.service.order.OrderService;
import com.project.orderservice.service.OrderStatusUpdateResult;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacadeImpl implements OrderFacade {

  private final OrderService orderService;

  private final OrderMapper orderMapper;

  private final OrderStatusChangePublisher orderStatusChangePublisher;

  @Override
  public OrderResponseDto createOrder(CreateOrderDto req) {
    Order order = orderMapper.createOrderDtoToOrder(req);
    order = orderService.createOrder(order, req.customerId(), req.productIds());
    return orderMapper.orderToOrderResponseDto(order);
  }

  @Override
  public OrderResponseDto getOrder(Long orderId) {
    Order order = orderService.getOrder(orderId);

    return orderMapper.orderToOrderResponseDto(order);
  }

  @Override
  public PageResponseDto<OrderSummaryDto> listOrders(Status status, Long customerId,
      Pageable pageable) {
    Page<Order> page = orderService.findOrders(status, customerId, pageable);
    if (page.isEmpty()) {
      return orderMapper.toPageResponseDto(page, List.of());
    }

    Map<Long, Order> hydrated = orderService.loadWithDetailsById(page);
    List<OrderSummaryDto> content = page.getContent().stream()
        .map(order -> hydrated.getOrDefault(order.getId(), order))
        .map(orderMapper::orderToOrderSummaryDto)
        .toList();
    return orderMapper.toPageResponseDto(page, content);
  }

  /**
   * Announcing the change is deliberately a second step, taken after {@code updateOrderStatus} has
   * returned and its transaction has committed: the customer is emailed and the analytics webhook
   * fires only about a change that is already durable.
   */
  @Override
  public OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusDto req, Jwt jwt) {
    OrderStatusUpdateResult result =
        orderService.updateOrderStatus(orderId, attributeToCaller(req, jwt));
    result.change().ifPresent(orderStatusChangePublisher::publish);
    return orderMapper.orderToOrderResponseDto(result.order());
  }

  /**
   * Announced exactly like an operator's change, and for the same reason: the reporting system is
   * told the order moved only once that move is durable here.
   */
  @Override
  public OrderResponseDto applyExternalStatusUpdate(
      Long orderId, ExternalStatusUpdateDto req, Jwt jwt) {
    OrderStatusUpdateResult result =
        orderService.applyExternalStatusUpdate(orderId, trustOurClock(req), reporterOf(jwt));
    result.change().ifPresent(orderStatusChangePublisher::publish);
    return orderMapper.orderToOrderResponseDto(result.order());
  }

  /**
   * Names the calling system from its token: {@code client_id} for a client-credentials token, the
   * subject when AuthService issued it without one. Never the request body — a machine caller has
   * no field to name itself with, which is what keeps a leaked integration credential from signing
   * a transition as an operator.
   */
  private String reporterOf(Jwt jwt) {
    String clientId = jwt.getClaimAsString("client_id");
    return clientId == null ? jwt.getSubject() : clientId;
  }

  /**
   * Keeps a reported timestamp only while it is not in the future. A partner's clock running fast
   * would otherwise date a transition ahead of the one that follows it, and both the customer email
   * and the webhook subscribers order events by exactly this field. A timestamp that is merely old
   * is kept: a callback delayed by a retry is honestly describing when the change happened.
   */
  private ExternalStatusUpdateDto trustOurClock(ExternalStatusUpdateDto req) {
    Instant reported = req.occurredAt();
    if (reported == null || !reported.isAfter(Instant.now())) {
      return req;
    }
    log.warn("System reported order status at {}, which is in the future; using arrival time",
        reported);
    return new ExternalStatusUpdateDto(req.status(), req.reason(), req.externalReference(), null);
  }

  /**
   * Fills {@code changedBy} from the token when the caller left it empty, so the customer email and
   * the analytics webhook name whoever actually made the change instead of a generic "admin-api". A
   * value the caller did send is kept: a console calling on an operator's behalf knows the
   * operator's name, and its own token does not.
   */
  private UpdateOrderStatusDto attributeToCaller(UpdateOrderStatusDto req, Jwt jwt) {
    if (req.changedBy() != null && !req.changedBy().isBlank()) {
      return req;
    }
    String caller = jwt.getClaimAsString("email");
    if (caller == null) {
      // A third-party system has no email; its client id is the closest thing to a name.
      caller = jwt.getClaimAsString("client_id");
    }
    return new UpdateOrderStatusDto(req.status(), req.reason(),
        caller == null ? jwt.getSubject() : caller);
  }
}
