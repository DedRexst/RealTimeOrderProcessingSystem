package com.project.analyticsservice.dto;

/**
 * Time orders spend in {@code fromStatus} before moving to {@code toStatus}.
 *
 * @param samples number of transitions the figures are based on
 * @param lastSeconds duration of the transition that produced this update
 */
public record TransitionTimeDto(
    String fromStatus,
    String toStatus,
    long samples,
    double avgSeconds,
    double maxSeconds,
    double lastSeconds
) {}
