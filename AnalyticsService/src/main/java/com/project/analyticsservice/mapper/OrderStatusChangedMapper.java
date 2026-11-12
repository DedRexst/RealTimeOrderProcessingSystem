package com.project.analyticsservice.mapper;

import com.project.analyticsservice.dto.OrderStatusChangedDto;
import com.project.analyticsservice.event.OrderStatusChangedEvent;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedMapper {

  public OrderStatusChangedDto eventToDto(OrderStatusChangedEvent event) {
    return new OrderStatusChangedDto(
        event.getOrderId(),
        event.getCustomerId(),
        event.getPreviousStatus(),
        event.getNewStatus(),
        event.getCurrency(),
        event.getProductIds(),
        event.getReason(),
        event.getChangedBy(),
        event.getExternalReference(),
        event.getOccurredAt());
  }
}
