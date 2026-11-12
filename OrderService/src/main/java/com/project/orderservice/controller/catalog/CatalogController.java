package com.project.orderservice.controller.catalog;

import com.project.orderservice.controller.catalog.facade.CatalogFacade;
import com.project.orderservice.dto.CreateProductDto;
import com.project.orderservice.dto.CustomerRefDto;
import com.project.orderservice.dto.ProductRefDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The two lists an order form needs to be usable by a person rather than by curl: which products
 * exist, and which customers they can be ordered for. {@code POST /orders} takes bare ids, and
 * without these there is nowhere for a UI to learn what id 7 is called.
 *
 * <p>Unpaged. The seeded catalogue is ten products and fifteen customers; if either
 * grows to the point where that is the wrong answer, this is the place that has to gain a page
 * parameter, and the cap on {@code GET /orders} is the precedent for how.
 *
 * <p>The two GET routes fall under {@code anyRequest().authenticated()} — no separate rule in
 * {@link com.project.orderservice.configuration.SecurityConfig}, because neither reveals more than
 * the order endpoints already do. {@link CustomerRefDto} is trimmed to the fields a dropdown uses
 * for exactly that reason. Adding a product is ADMIN-only, ruled in SecurityConfig.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog")
public class CatalogController {

  private final CatalogFacade catalogFacade;

  @GetMapping("/products")
  @Transactional(value = "transactionManager", readOnly = true)
  public List<ProductRefDto> products() {
    return catalogFacade.findAllProducts();
  }

  @GetMapping("/customers")
  @Transactional(value = "transactionManager", readOnly = true)
  public List<CustomerRefDto> customers() {
    return catalogFacade.findAllCustomers();
  }

  @PostMapping("/products")
  @ResponseStatus(HttpStatus.CREATED)
  public ProductRefDto createProduct(@Valid @RequestBody CreateProductDto request) {
    return catalogFacade.createProduct(request);
  }
}
