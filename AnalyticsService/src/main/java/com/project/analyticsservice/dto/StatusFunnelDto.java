package com.project.analyticsservice.dto;

import java.util.List;

/**
 * How many distinct orders have reached each status, largest first. Always sent whole, because a
 * new order changes every share.
 */
public record StatusFunnelDto(long totalOrders, List<Stage> stages) {

  public record Stage(String status, long orders, double sharePercent) {}
}
