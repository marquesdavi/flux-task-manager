package com.todo.flux.module.board.dto;

import jakarta.validation.constraints.NotBlank;

public record BoardUpdateRequest(
        @NotBlank
        String title
) {
}
