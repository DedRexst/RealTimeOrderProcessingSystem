package com.project.orderservice.controller.catalog.facade.mapper;

import com.project.orderservice.dto.CustomerRefDto;
import com.project.orderservice.dto.ProductRefDto;
import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Product;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {
  public CustomerRefDto toCustomerRefDto(Customer customer) {
    return new CustomerRefDto(customer.getId(), customer.getName(), customer.getAddress());
  }

  public ProductRefDto toProductRefDto(Product product) {
    return new ProductRefDto(product.getId(), product.getName(), product.getDescription());
  }
}
