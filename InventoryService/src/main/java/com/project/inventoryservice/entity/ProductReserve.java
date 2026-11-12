package com.project.inventoryservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(
    name = "product_reserve",
    // One reservation per order: the database is the last line of defence for consumer
    // idempotency when a duplicate OrderCreatedEvent slips past the existsByOrderId check.
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_reserve_order_id",
        columnNames = "order_id"))
public class ProductReserve extends BaseEntity {

  @ManyToMany(targetEntity = Product.class, cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
  @JoinTable(
      name = "product_product_reserve",
      joinColumns = {@JoinColumn(name = "product_reserve_id")},
      inverseJoinColumns = {@JoinColumn(name = "product_id")})
  private List<Product> product;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "quantity", nullable = false)
  private Long quantity;
}
