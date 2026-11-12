package com.project.analyticsservice.dto;

public record CustomerStatsDto(
    String customerId,
    long orderCount,
    long productCount,
    double avgProductsPerOrder
) {}
