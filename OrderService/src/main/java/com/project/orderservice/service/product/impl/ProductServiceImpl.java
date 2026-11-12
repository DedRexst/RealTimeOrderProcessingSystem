package com.project.orderservice.service.product.impl;

import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Product;
import com.project.orderservice.event.ProductCreatedEvent;
import com.project.orderservice.reporsitory.CustomerRepository;
import com.project.orderservice.reporsitory.ProductRepository;
import com.project.orderservice.service.outbox.OutboxService;
import com.project.orderservice.service.product.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private static final Sort BY_ID = Sort.by(Sort.Direction.ASC, "id");

  private final ProductRepository productRepository;
  private final CustomerRepository customerRepository;
  private final OutboxService outboxService;

  @Override
  public List<Product> findAllProductsOrderedById() {
    return productRepository.findAll(BY_ID);
  }

  @Override
  public List<Customer> findAllCustomerOrderedById() {
    return customerRepository.findAll(BY_ID);
  }

  @Override
  @Transactional("transactionManager")
  public Product createProduct(String name, String description, long initialStock) {
    Product product = productRepository.save(Product.builder()
        .name(name)
        .description(description)
        .build());

    outboxService.enqueueProductCreated(ProductCreatedEvent.builder()
        .productId(product.getId())
        .name(product.getName())
        .initialStock(initialStock)
        .build());

    log.info("Created product id={}, name={}, initialStock={}",
        product.getId(), product.getName(), initialStock);
    return product;
  }
}
