package com.project.orderservice.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * A page of results in a shape this service owns.
 *
 * <p>Spring's own {@code Page} is deliberately not returned from a controller: its JSON is an
 * implementation detail of Spring Data that has already changed shape once between versions, and
 * a frontend written against it breaks on an upgrade that touches no code here.
 */
public record PageResponseDto<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
