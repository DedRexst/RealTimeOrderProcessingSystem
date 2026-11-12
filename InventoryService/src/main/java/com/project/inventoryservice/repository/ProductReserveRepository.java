package com.project.inventoryservice.repository;

import com.project.inventoryservice.entity.ProductReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReserveRepository extends JpaRepository<ProductReserve, Long> {

  boolean existsByOrderId(Long orderId);
}
