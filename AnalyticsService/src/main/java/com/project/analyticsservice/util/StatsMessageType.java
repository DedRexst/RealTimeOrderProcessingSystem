package com.project.analyticsservice.util;

/** The {@code type} field of every message sent to the dashboard and the webhook. */
public enum StatsMessageType {
  CUSTOMER_STATS,
  ORDER_RATE,
  CURRENCY_BREAKDOWN,
  PRODUCT_POPULARITY,
  ORDER_STATUS_CHANGED,
  STATUS_TRANSITION_TIME,
  STATUS_FUNNEL,
  STATUS_REASONS
}
