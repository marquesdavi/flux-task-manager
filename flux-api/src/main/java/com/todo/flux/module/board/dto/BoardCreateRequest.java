package com.todo.flux.module.board.dto;

import jakarta.validation.constraints.NotBlank;

public record BoardCreateRequest(
        @NotBlank
        String title
) {
}
