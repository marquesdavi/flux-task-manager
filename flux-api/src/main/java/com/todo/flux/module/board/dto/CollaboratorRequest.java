package com.todo.flux.module.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CollaboratorRequest(
        @NotBlank @Email
        String email
) {}
