package com.todo.flux.module.user.dto;

public record UserSummary(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
