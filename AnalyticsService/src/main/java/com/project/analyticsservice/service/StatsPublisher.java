package com.project.analyticsservice.service;

import com.project.analyticsservice.avro.CustomerOrderStats;
import com.project.analyticsservice.dto.OrderStatusChangedDto;
import com.project.analyticsservice.dto.StatusAnalyticsUpdate;

/** Sends analytics to the connected dashboards and, when configured, to the webhook. */
public interface StatsPublisher {

  void publishCustomerStats(String customerId, CustomerOrderStats stats);

  void publishOrderRate(String windowStart, Long orderCount);

  void publishCurrencyBreakdown(String currency, Long orderCount);

  void publishProductPopularity(String productId, Long orderCount);

  void publishStatusAnalytics(StatusAnalyticsUpdate update);

  void publishOrderStatusChanged(OrderStatusChangedDto statusChanged);
}
