package com.project.inventoryservice.service;

import com.project.inventoryservice.event.InventoryReservedEvent;

public interface InventoryService {
  void reserve(InventoryReservedEvent event);

  /**
   * Gives a newly catalogued product its starting stock.
   *
   * @return false when the product already has a stock row, which is left as it is
   */
  boolean registerProduct(Long productId, long initialStock);
}
