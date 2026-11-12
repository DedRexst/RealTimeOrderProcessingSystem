package com.project.analyticsservice.dto;

/**
 * What one status transition changed. The funnel is always present; transitionTime is null when
 * no duration could be measured and reasons is null when the transition had no reason.
 */
public record StatusAnalyticsUpdate(
    TransitionTimeDto transitionTime,
    StatusFunnelDto funnel,
    StatusReasonsDto reasons
) {}
