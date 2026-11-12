package com.project.analyticsservice.dto;

import java.util.List;

/** The most frequent reasons given for moving an order into {@code status}. */
public record StatusReasonsDto(String status, long totalWithReason, List<ReasonCount> topReasons) {

  public record ReasonCount(String reason, long count) {}
}
