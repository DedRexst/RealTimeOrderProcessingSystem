package com.project.analyticsservice.service.impl;

import com.project.analyticsservice.avro.CustomerOrderStats;
import com.project.analyticsservice.client.WebhookClient;
import com.project.analyticsservice.dto.CurrencyBreakdownDto;
import com.project.analyticsservice.dto.CustomerStatsDto;
import com.project.analyticsservice.dto.OrderRateDto;
import com.project.analyticsservice.dto.OrderStatusChangedDto;
import com.project.analyticsservice.dto.ProductPopularityDto;
import com.project.analyticsservice.dto.StatusAnalyticsUpdate;
import com.project.analyticsservice.dto.StatusReasonsDto;
import com.project.analyticsservice.dto.TransitionTimeDto;
import com.project.analyticsservice.service.StatsPublisher;
import com.project.analyticsservice.util.StatsMessageType;
import com.project.analyticsservice.websocket.StatsWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsPublisherImpl implements StatsPublisher {

  private static final String STATUS_FUNNEL_KEY = "ALL";

  private final StatsWebSocketHandler dashboard;
  private final WebhookClient webhookClient;

  @Override
  public void publishCustomerStats(String customerId, CustomerOrderStats stats) {
    double avgProductsPerOrder = stats.getOrderCount() > 0
        ? (double) stats.getProductCount() / stats.getOrderCount()
        : 0.0;

    publishAggregate(StatsMessageType.CUSTOMER_STATS, customerId, new CustomerStatsDto(
        customerId, stats.getOrderCount(), stats.getProductCount(), avgProductsPerOrder));
  }

  @Override
  public void publishOrderRate(String windowStart, Long orderCount) {
    publishAggregate(StatsMessageType.ORDER_RATE, windowStart,
        new OrderRateDto(windowStart, orderCount));
  }

  @Override
  public void publishCurrencyBreakdown(String currency, Long orderCount) {
    publishAggregate(StatsMessageType.CURRENCY_BREAKDOWN, currency,
        new CurrencyBreakdownDto(currency, orderCount));
  }

  @Override
  public void publishProductPopularity(String productId, Long orderCount) {
    publishAggregate(StatsMessageType.PRODUCT_POPULARITY, productId,
        new ProductPopularityDto(productId, orderCount));
  }

  @Override
  public void publishStatusAnalytics(StatusAnalyticsUpdate update) {
    TransitionTimeDto transitionTime = update.transitionTime();
    if (transitionTime != null) {
      publishAggregate(StatsMessageType.STATUS_TRANSITION_TIME,
          transitionTime.fromStatus() + "->" + transitionTime.toStatus(), transitionTime);
    }

    publishAggregate(StatsMessageType.STATUS_FUNNEL, STATUS_FUNNEL_KEY, update.funnel());

    StatusReasonsDto reasons = update.reasons();
    if (reasons != null) {
      publishAggregate(StatsMessageType.STATUS_REASONS, reasons.status(), reasons);
    }
  }

  @Override
  public void publishOrderStatusChanged(OrderStatusChangedDto statusChanged) {
    dashboard.publish(StatsMessageType.ORDER_STATUS_CHANGED, null, statusChanged);
    webhookClient.send(StatsMessageType.ORDER_STATUS_CHANGED, statusChanged);
  }

  /**
   * Webhook failures are logged and swallowed: these run on Kafka Streams threads, where a throw
   * kills the stream, and the next aggregate carries the same information anyway.
   */
  private void publishAggregate(StatsMessageType type, String snapshotKey, Object data) {
    dashboard.publish(type, snapshotKey, data);
    try {
      webhookClient.send(type, data);
    } catch (Exception e) {
      log.error("Webhook delivery failed: type={}", type, e);
    }
  }
}
