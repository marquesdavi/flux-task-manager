package com.todo.flux.module.auth.service.impl;

import com.todo.flux.config.resilience.Resilient;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl
        implements AuthenticationService<User, LoginRequest, TokenResponse> {
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final UserService<User, RegisterRequest> userService;

    @Value("${jwt.token.expires-in:3600}")
    private long expiresIn;
    private static final String ISSUER = "flux-api";

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public TokenResponse authenticate(LoginRequest request) {
        log.info("Tentativa de login para o email: {}", request.email());
        User user = userService.findByEmail(request.email());

        if (Objects.isNull(user) || !isPasswordCorrect(request.password(), user.getPassword())) {
            log.warn("Falha na tentativa de login para o email: {}", request.email());
            throw new BadCredentialsException("Usuário ou senha inválidos!");
        }
        return generateResponse(user);
    }

    private boolean isPasswordCorrect(String requestPassword, String userPassword) {
        return passwordEncoder.matches(requestPassword, userPassword);
    }

    @Override
    public TokenResponse generateResponse(User user) {
        JwtClaimsSet claims = buildJwtClaimsSet(user);
        String jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenResponse(jwtValue, expiresIn);
    }

    private JwtClaimsSet buildJwtClaimsSet(User user) {
        Instant now = Instant.now();

        return JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("role", user.getRole().name())
                .build();
    }

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public User getAuthenticated(){
        Authentication currentSession = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(currentSession.getName());
        return userService.findByIdOrElseThrow(userId);
    }
}
