package com.project.inventoryservice.repository;

import com.project.inventoryservice.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Locks the row for the duration of the transaction so that two concurrent reservations cannot
   * both read the same stock level and each decrement it (lost update). Callers must lock products
   * in a deterministic order — ascending id — to avoid deadlocking against each other.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
