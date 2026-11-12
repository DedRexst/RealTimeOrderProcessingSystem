package com.project.orderservice.reporsitory;

import com.project.orderservice.enity.OutboxEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  /**
   * The oldest unpublished events, locked for the rest of the caller's transaction.
   *
   * <p>{@code SKIP LOCKED} lets several instances of this service relay at once: each takes rows
   * the others are not holding, so no event is sent twice by two relays racing for it.
   */
  @Query(value = """
      SELECT * FROM outbox_event
      WHERE published_at IS NULL
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
      """, nativeQuery = true)
  List<OutboxEvent> lockUnpublished(@Param("limit") int limit);

  @Modifying
  @Query("DELETE FROM OutboxEvent e WHERE e.publishedAt < :before")
  int deletePublishedBefore(@Param("before") Instant before);
}
