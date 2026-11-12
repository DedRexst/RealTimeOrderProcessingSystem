package com.project.orderservice.enity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "orders")
public class Order extends BaseEntity{

  @ManyToOne(targetEntity = Customer.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id")
  private Customer customer;

  @ManyToMany(targetEntity = Product.class, cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
  @JoinTable(
      name = "order_product",
      joinColumns = {@JoinColumn(name = "order_id")},
      inverseJoinColumns = {@JoinColumn(name = "product_id")})
  private List<Product> products;

  // columnDefinition, not a plain length: left to itself Hibernate maps @Enumerated(STRING) onto
  // H2's native ENUM type, whose permitted values are fixed at CREATE TABLE and never widened by
  // ddl-auto=update — that is what made adding Status.PENDING fail with 22030 against an existing
  // database. Naming the type outright also suppresses the "check (status in (...))" constraint
  // Hibernate would otherwise emit, which would freeze the same list one layer up. Adding an enum
  // constant is now purely a code change.
  @Column(name = "currency", columnDefinition = "varchar(32)")
  @Enumerated(EnumType.STRING)
  private Currency currency;

  @Column(name = "order_address")
  private String orderAddress;

  @Column(name = "status", columnDefinition = "varchar(32)")
  @Enumerated(EnumType.STRING)
  private Status status;
}
