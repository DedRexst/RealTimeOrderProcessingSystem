package com.project.orderservice.service;

import java.time.Instant;

/**
 * Everything about one status transition except the order it happened to: who caused it, why, and —
 * when a third-party system reported it — what record on their side it came from and when it
 * happened there.
 *
 * <p>Carried as one value rather than four parameters because both event payloads need all of it,
 * and because a transition reported from outside differs from an operator's only in these fields.
 *
 * @param changedBy an operator login, a machine client id, or {@code inventory-service} for the
 *     saga's own transitions
 * @param reason free text explaining the move; null when the caller said nothing
 * @param externalReference the id of the transition's record in the reporting system; null for
 *     anything that originated here
 * @param occurredAt when the change happened at its source; null means "as this is written"
 */
public record StatusChangeDetails(
    String changedBy,
    String reason,
    String externalReference,
    Instant occurredAt
) {

  /** A transition made here, which has no external record and happens as it is recorded. */
  public static StatusChangeDetails internal(String changedBy, String reason) {
    return new StatusChangeDetails(changedBy, reason, null, null);
  }
}
