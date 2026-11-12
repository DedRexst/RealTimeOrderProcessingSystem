package com.project.analyticsservice.dto;

public record CurrencyBreakdownDto(
    String currency,
    long orderCount
) {}
