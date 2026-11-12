package com.project.orderservice.controller.order.facade.mapper;

import com.project.orderservice.dto.CreateOrderDto;
import com.project.orderservice.dto.OrderResponseDto;
import com.project.orderservice.dto.OrderSummaryDto;
import com.project.orderservice.dto.PageResponseDto;
import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Order;
import com.project.orderservice.enity.Product;
import com.project.orderservice.enity.Status;
import com.project.orderservice.event.OrderCreatedEvent;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;


@Component
public class OrderMapper {

  public Order createOrderDtoToOrder(CreateOrderDto req) {
    return Order.builder()
        .orderAddress(req.address())
        .currency(req.currency())
        .status(Status.PENDING)
        .build();
  }

  public OrderSummaryDto orderToOrderSummaryDto(Order order) {
    Customer customer = order.getCustomer();
    return new OrderSummaryDto(
        order.getId(),
        customer == null ? null : customer.getId(),
        customer == null ? null : customer.getName(),
        order.getProducts().stream().map(Product::getId).toList(),
        order.getCurrency(),
        order.getOrderAddress(),
        order.getStatus(),
        order.getCreatedTime(),
        order.getUpdatedTime()
    );
  }

  public OrderResponseDto orderToOrderResponseDto(Order order) {
    return new OrderResponseDto(
        order.getId(),
        order.getCustomer().getId(),
        order.getProducts().stream().map(Product::getId).toList(),
        order.getCurrency(),
        order.getOrderAddress(),
        order.getStatus()
    );
  }

  public PageResponseDto<OrderSummaryDto> toPageResponseDto(Page<Order> page, List<OrderSummaryDto> content) {
    return new PageResponseDto<>(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
