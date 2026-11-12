package com.project.orderservice.controller.catalog.facade;

import com.project.orderservice.dto.CreateProductDto;
import com.project.orderservice.dto.CustomerRefDto;
import com.project.orderservice.dto.ProductRefDto;
import java.util.List;

public interface CatalogFacade {
  List<ProductRefDto> findAllProducts();

  List<CustomerRefDto> findAllCustomers();

  ProductRefDto createProduct(CreateProductDto request);
}
