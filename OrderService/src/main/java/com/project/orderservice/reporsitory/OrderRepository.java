package com.project.orderservice.reporsitory;

import com.project.orderservice.enity.Order;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
    JpaSpecificationExecutor<Order> {

  /**
   * Products and the customer are both LAZY; fetch them eagerly so the order can be mapped outside
   * the session, and so building a notification payload costs one query rather than three.
   */
  @Query("select o from Order o left join fetch o.products left join fetch o.customer"
      + " where o.id = :id")
  Optional<Order> findByIdWithProducts(@Param("id") Long id);

  /**
   * The second half of a paged listing: the page itself is selected without the collections, and
   * this hydrates exactly those rows in one query.
   *
   * <p>A fetch join cannot be paged — the join multiplies rows per product, so the database's LIMIT
   * would cut an order in half and Hibernate falls back to paging the whole result set in memory.
   * Selecting the ids first and fetching them here costs one extra query and keeps the limit where
   * it belongs. Returned in no particular order; the caller restores the page's.
   *
   * <p>Deliberately not {@code select distinct}. An order carries one join-table row per unit
   * ordered — two of the same product is two identical rows — and DISTINCT reaches the SQL, where
   * it collapses them and silently turns a quantity of two into one. The duplicate root rows it
   * would have removed cost nothing instead: Hibernate returns the same instance for each, so the
   * caller's map keyed by id folds them back together.
   */
  @Query("select o from Order o left join fetch o.products left join fetch o.customer"
      + " where o.id in :ids")
  List<Order> findAllByIdWithProducts(@Param("ids") Collection<Long> ids);
}
