package com.project.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "product")
public class Product {
  @Id
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  /**
   * Guards against lost updates on concurrent stock changes. Reservations additionally take a
   * PESSIMISTIC_WRITE lock (see {@code ProductRepository#findByIdForUpdate}), so this is a
   * second line of defence for any write path that reads without locking.
   */
  @Version
  @EqualsAndHashCode.Exclude
  @Column(name = "version")
  private Long version;
}
