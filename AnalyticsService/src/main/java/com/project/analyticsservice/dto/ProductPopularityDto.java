package com.project.analyticsservice.dto;

public record ProductPopularityDto(
    String productId,
    long orderCount
) {}
