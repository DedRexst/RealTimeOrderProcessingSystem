package com.project.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tells InventoryService that a product now exists. Without it a new product could be added to an
 * order but never reserved, since InventoryService only knows the ids it holds stock for.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {

  private Long productId;

  private String name;

  private Long initialStock;
}
