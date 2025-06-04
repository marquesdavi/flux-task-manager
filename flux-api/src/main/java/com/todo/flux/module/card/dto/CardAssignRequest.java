package com.todo.flux.module.card.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CardAssignRequest(
        @NotBlank @Email
        String email
) {}
