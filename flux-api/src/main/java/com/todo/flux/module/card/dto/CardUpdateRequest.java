package com.todo.flux.module.card.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CardUpdateRequest(
        @NotBlank
        String title,
        String descriptionBrief,
        String descriptionFull,
        @NotBlank
        String status,      // “To Do” | “Doing” | “Done”
        LocalDate startDate,
        LocalDate endDate,
        LocalDate dueDate,
        String imageUrl
) {
}
