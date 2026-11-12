package com.project.orderservice.service.product;

import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Product;
import java.util.List;

public interface ProductService {
  List<Product> findAllProductsOrderedById();

  List<Customer> findAllCustomerOrderedById();

  Product createProduct(String name, String description, long initialStock);
}
