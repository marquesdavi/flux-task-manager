package com.todo.flux.module.board.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        String title,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
