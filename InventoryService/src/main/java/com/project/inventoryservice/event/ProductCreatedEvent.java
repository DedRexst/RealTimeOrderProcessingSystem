package com.project.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A product OrderService just added to its catalogue, with the stock it starts with. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {
    private Long productId;

    private String name;

    private Long initialStock;
}
