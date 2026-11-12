package com.project.analyticsservice.dto;

public record OrderRateDto(
    String windowKey,
    long orderCount
) {}
