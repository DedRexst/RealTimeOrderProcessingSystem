package com.project.analyticsservice.service;

import com.project.analyticsservice.dto.StatusAnalyticsUpdate;
import com.project.analyticsservice.event.OrderStatusChangedEvent;
import java.util.Optional;

public interface OrderStatusAnalyticsService {

  Optional<StatusAnalyticsUpdate> record(OrderStatusChangedEvent event);
}
