package com.todo.flux.module.card.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String title,
        String descriptionBrief,
        String descriptionFull,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate dueDate,
        String imageUrl,
        UUID boardId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
