package com.todo.flux.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @Email
        @NotNull
        @NotBlank
        String email,
        @NotNull
        @NotBlank
        String password) {
}
