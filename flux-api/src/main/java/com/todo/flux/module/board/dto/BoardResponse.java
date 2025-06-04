package com.todo.flux.module.board.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        String title,
        Long ownerId,
        List<String> collaboratorsEmails,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
