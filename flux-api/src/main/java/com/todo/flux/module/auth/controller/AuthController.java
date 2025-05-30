package com.todo.flux.module.auth.controller;

import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.module.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "User Authentication", description = "User Authentication management")
public class AuthController {
    private final AuthenticationService<User, LoginRequest, TokenResponse> service;

    @Operation(summary = "Returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(service.authenticate(loginRequest));
    }
}
