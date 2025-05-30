package com.todo.flux.module.auth.dto;

public record TokenResponse(String accessToken, Long expiresIn) {
}
