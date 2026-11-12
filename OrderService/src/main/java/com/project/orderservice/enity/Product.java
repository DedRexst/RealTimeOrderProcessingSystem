package com.project.orderservice.enity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
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
@EqualsAndHashCode()
@Table(name = "product")
public class Product extends BaseEntity {
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "decription")
  private String description;

  @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
  private List<Order> orders;
}
