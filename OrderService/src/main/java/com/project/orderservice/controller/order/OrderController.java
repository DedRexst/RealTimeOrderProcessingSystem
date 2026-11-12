package com.project.orderservice.controller.order;

import com.project.orderservice.dto.CreateOrderDto;
import com.project.orderservice.dto.ExternalStatusUpdateDto;
import com.project.orderservice.dto.OrderResponseDto;
import com.project.orderservice.dto.OrderSummaryDto;
import com.project.orderservice.dto.PageResponseDto;
import com.project.orderservice.dto.UpdateOrderStatusDto;
import com.project.orderservice.enity.Status;
import com.project.orderservice.controller.order.facade.OrderFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

  private static final int MAX_PAGE_SIZE = 100;

  private final OrderFacade orderFacade;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public OrderResponseDto createOrder(@Valid @RequestBody CreateOrderDto req) {
    return orderFacade.createOrder(req);
  }

  @GetMapping
  public PageResponseDto<OrderSummaryDto> listOrders(
      @RequestParam(required = false) Status status,
      @RequestParam(required = false) Long customerId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    return orderFacade.listOrders(status, customerId,
        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id")));
  }

  @GetMapping("/{id}")
  public OrderResponseDto getOrder(@PathVariable Long id) {
    return orderFacade.getOrder(id);
  }

  @PatchMapping("/{id}/status")
  public OrderResponseDto updateOrderStatus(@PathVariable Long id,
      @Valid @RequestBody UpdateOrderStatusDto req, @AuthenticationPrincipal Jwt jwt) {

    return orderFacade.updateOrderStatus(id, req, jwt);
  }

  @PostMapping("/{id}/status-callbacks")
  public OrderResponseDto reportStatus(@PathVariable Long id,
      @Valid @RequestBody ExternalStatusUpdateDto req, @AuthenticationPrincipal Jwt jwt) {
    return orderFacade.applyExternalStatusUpdate(id, req, jwt);
  }
}
