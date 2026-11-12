package com.project.analyticsservice.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Consumer-side copy of the event OrderService publishes to orders.created. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

  private Long orderId;
  private Long customerId;
  private List<Long> productId;
  private String currency;
}
