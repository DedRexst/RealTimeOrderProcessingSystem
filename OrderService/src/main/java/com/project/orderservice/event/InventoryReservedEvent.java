package com.project.orderservice.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound counterpart of InventoryService's event of the same name. Field names must stay in step
 * with the producer: {@code inventory.reserved} carries no type headers, so the payload is bound
 * purely by name against spring.json.value.default.type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {

  private Long orderId;

  private List<Long> productId;
}
