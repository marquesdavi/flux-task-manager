package com.todo.flux.module.card.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CardUpdateRequest(
        @NotBlank
        String title,
        String descriptionBrief,
        String descriptionFull,
        @NotBlank
        String status,
        String priority,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate dueDate,
        String imageUrl
) {
}
