package com.project.orderservice.controller.catalog.facade.impl;

import com.project.orderservice.controller.catalog.facade.CatalogFacade;
import com.project.orderservice.controller.catalog.facade.mapper.CatalogMapper;
import com.project.orderservice.dto.CreateProductDto;
import com.project.orderservice.dto.CustomerRefDto;
import com.project.orderservice.dto.ProductRefDto;
import com.project.orderservice.service.product.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogFacadeImpl implements CatalogFacade {
  private final ProductService productService;
  private final CatalogMapper catalogMapper;

  @Override
  public List<ProductRefDto> findAllProducts() {
    return productService.findAllProductsOrderedById().stream()
        .map(catalogMapper::toProductRefDto)
        .toList();
  }

  @Override
  public List<CustomerRefDto> findAllCustomers() {
    return productService.findAllCustomerOrderedById().stream()
        .map(catalogMapper::toCustomerRefDto)
        .toList();
  }

  @Override
  public ProductRefDto createProduct(CreateProductDto request) {
    String description = request.description() == null || request.description().isBlank()
        ? null : request.description().trim();
    return catalogMapper.toProductRefDto(productService.createProduct(
        request.name().trim(), description, request.initialStock()));
  }
}
