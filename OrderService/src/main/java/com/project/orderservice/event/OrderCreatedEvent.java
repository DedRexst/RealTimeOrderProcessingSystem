package com.project.orderservice.event;

import com.project.orderservice.enity.Currency;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
// OutboxRelay reads the event back from its stored JSON, which needs a constructor to bind into.
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;

    private Long customerId;

    private List<Long> productId;

    private Currency currency;
}
