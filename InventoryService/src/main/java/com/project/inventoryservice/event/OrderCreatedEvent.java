package com.project.inventoryservice.event;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private Long customerId;
    private List<Long> productId;
    private Currency currency;
}
