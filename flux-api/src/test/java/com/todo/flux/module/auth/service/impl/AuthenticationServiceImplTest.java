package com.todo.flux.module.auth.service.impl;

import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.entity.RoleEnum;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.module.user.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Tests")
class AuthenticationServiceImplTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UserService<User, RegisterRequest> userService;

    @Mock
    private Jwt mockJwt;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private LoginRequest loginRequest;

    private final long testExpiresIn = 3600L;
    private final String testEmail = "test@example.com";
    private final String testRawPassword = "password123";
    private final String testEncodedPassword = "encodedPassword123";
    private final Long testUserId = 1L;
    private final String testJwtTokenValue = "test.jwt.token.value";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "expiresIn", testExpiresIn);

        testUser = User.builder()
                .id(testUserId)
                .email(testEmail)
                .password(testEncodedPassword)
                .firstName("Test")
                .lastName("User")
                .role(RoleEnum.USER)
                .build();

        loginRequest = new LoginRequest(testEmail, testRawPassword);
    }

    @Nested
    @DisplayName("authenticate method tests")
    class Authenticate {

        @Test
        @DisplayName("should return TokenResponse when authentication is successful")
        void shouldReturnTokenResponse_whenAuthenticationIsSuccessful() {
            when(userService.findByEmail(testEmail)).thenReturn(testUser);
            when(passwordEncoder.matches(testRawPassword, testEncodedPassword)).thenReturn(true);
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);
            when(mockJwt.getTokenValue()).thenReturn(testJwtTokenValue);

            TokenResponse tokenResponse = authenticationService.authenticate(loginRequest);

            assertNotNull(tokenResponse);
            assertEquals(testJwtTokenValue, tokenResponse.accessToken());
            assertEquals(testExpiresIn, tokenResponse.expiresIn());

            verify(userService, times(1)).findByEmail(testEmail);
            verify(passwordEncoder, times(1)).matches(testRawPassword, testEncodedPassword);
            verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("should throw BadCredentialsException when user is not found")
        void shouldThrowBadCredentialsException_whenUserNotFound() {
            when(userService.findByEmail(testEmail)).thenReturn(null);

            BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                    () -> authenticationService.authenticate(loginRequest));

            assertEquals("Usuário ou senha inválidos!", exception.getMessage());
            verify(userService, times(1)).findByEmail(testEmail);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("should throw BadCredentialsException when password does not match")
        void shouldThrowBadCredentialsException_whenPasswordDoesNotMatch() {
            when(userService.findByEmail(testEmail)).thenReturn(testUser);
            when(passwordEncoder.matches(testRawPassword, testEncodedPassword)).thenReturn(false);

            BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                    () -> authenticationService.authenticate(loginRequest));

            assertEquals("Usuário ou senha inválidos!", exception.getMessage());
            verify(userService, times(1)).findByEmail(testEmail);
            verify(passwordEncoder, times(1)).matches(testRawPassword, testEncodedPassword);
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }
    }

    @Nested
    @DisplayName("getAuthenticated method tests")
    class GetAuthenticated {

        private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

        @BeforeEach
        void getAuthenticatedSetUp() {
            mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
        }

        @AfterEach
        void tearDown() {
            if (mockedSecurityContextHolder != null) {
                mockedSecurityContextHolder.close();
            }
        }

        @Test
        @DisplayName("should return authenticated User when security context has valid authentication")
        void shouldReturnAuthenticatedUser_whenSecurityContextIsValid() {
            when(authentication.getName()).thenReturn(String.valueOf(testUserId));
            when(userService.findByIdOrElseThrow(testUserId)).thenReturn(testUser);

            User authenticatedUser = authenticationService.getAuthenticated();

            assertNotNull(authenticatedUser);
            assertEquals(testUser, authenticatedUser);
            verify(userService, times(1)).findByIdOrElseThrow(testUserId);
        }

        @Test
        @DisplayName("should throw NumberFormatException when authentication name is not a valid Long")
        void shouldThrowNumberFormatException_whenAuthNameIsNotValidLong() {
            when(authentication.getName()).thenReturn("not-a-long-id");

            assertThrows(NumberFormatException.class,
                    () -> authenticationService.getAuthenticated());
            verify(userService, never()).findByIdOrElseThrow(anyLong());
        }

        @Test
        @DisplayName("should throw NullPointerException when authentication is null in security context")
        void shouldThrowNullPointerException_whenAuthenticationIsNull() {
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThrows(NullPointerException.class,
                    () -> authenticationService.getAuthenticated());
            verify(userService, never()).findByIdOrElseThrow(anyLong());
        }
    }
}