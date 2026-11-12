package com.project.inventoryservice.service.impl;

import com.project.inventoryservice.entity.Product;
import com.project.inventoryservice.entity.ProductReserve;
import com.project.inventoryservice.event.InventoryReservedEvent;
import com.project.inventoryservice.repository.ProductRepository;
import com.project.inventoryservice.repository.ProductReserveRepository;
import com.project.inventoryservice.service.InventoryService;
import com.project.inventoryservice.util.BusinessException;
import com.project.inventoryservice.util.ErrorCode;
import com.project.inventoryservice.util.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
  private final ProductReserveRepository productReserveRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional
  public void reserve(InventoryReservedEvent event) {
    List<Long> requestedIds = event.getProductId();
    if (requestedIds == null || requestedIds.isEmpty()) {
      throw new BusinessException(ErrorCode.BSC3, event.getOrderId());
    }

    if (productReserveRepository.existsByOrderId(event.getOrderId())) {
      log.info("Reservation for orderId={} already exists, skipping duplicate event",
          event.getOrderId());
      return;
    }

    Map<Long, Long> amountByProductId = requestedIds.stream()
        .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));

    List<Product> products = amountByProductId.entrySet().stream()
        .map(entry -> reserveProduct(entry.getKey(), entry.getValue()))
        .toList();

    ProductReserve productReserve = ProductReserve.builder()
        .product(products)
        .orderId(event.getOrderId())
        .quantity((long) requestedIds.size())
        .build();
    productReserveRepository.save(productReserve);

    log.info("Reserved {} units across {} product(s) for orderId={}",
        requestedIds.size(), products.size(), event.getOrderId());
  }

  private Product reserveProduct(Long productId, long amount) {
    Product product = findProductById(productId);

    if (product.getQuantity() < amount) {
      throw new BusinessException(ErrorCode.BSC2, productId, amount, product.getQuantity());
    }

    product.setQuantity(product.getQuantity() - amount);
    return product;
  }

  private Product findProductById(Long productId) {
    return productRepository.findByIdForUpdate(productId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.BSC1, productId));
  }

  @Override
  @Transactional
  public boolean registerProduct(Long productId, long initialStock) {
    if (productRepository.existsById(productId)) {
      return false;
    }
    // version stays null so Spring Data persists rather than merges an entity with an assigned id.
    productRepository.save(Product.builder()
        .id(productId)
        .quantity(Math.max(initialStock, 0L))
        .build());
    log.info("Registered product {} with {} unit(s) in stock", productId, initialStock);
    return true;
  }
}
