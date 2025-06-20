package com.todo.flux.module.user.service.impl;

import com.todo.flux.exception.AlreadyExistsException;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCrudService Tests")
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password", "Test User", "Silva");
        user = new User(1L, "test@example.com", "password", "Test User", "Silva", null);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("shouldCreateUser_WhenEmailDoesNotExist")
        void shouldCreateUser_WhenEmailDoesNotExist() {
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0, User.class));

            userService.create(registerRequest);

            verify(userRepository, times(1)).existsByEmail(registerRequest.email());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("shouldThrowAlreadyExistsException_WhenEmailAlreadyExists")
        void shouldThrowAlreadyExistsException_WhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

            assertThrows(AlreadyExistsException.class, () -> userService.create(registerRequest));

            verify(userRepository, times(1)).existsByEmail(registerRequest.email());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("shouldThrowAlreadyExistsException_WhenEmailExists")
        void shouldThrowAlreadyExistsException_WhenEmailExists() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            assertThrows(AlreadyExistsException.class, () -> userService.existsByEmail("test@example.com"));
        }

        @Test
        @DisplayName("shouldNotThrowException_WhenEmailDoesNotExist")
        void shouldNotThrowException_WhenEmailDoesNotExist() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            assertDoesNotThrow(() -> userService.existsByEmail("test@example.com"));
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("shouldReturnUser_WhenUserExists")
        void shouldReturnUser_WhenUserExists() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            User foundUser = userService.findByEmail("test@example.com");
            assertEquals(user, foundUser);
        }

        @Test
        @DisplayName("shouldThrowNotFoundException_WhenUserDoesNotExist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> userService.findByEmail("test@example.com"));
        }
    }

    @Nested
    @DisplayName("list")
    class ListTests {

        @Test
        @DisplayName("shouldReturnAllUsers_WhenCalled")
        void shouldReturnAllUsers_WhenCalled() {
            List<User> users = List.of(user);
            when(userRepository.findAll()).thenReturn(users);
            List<User> result = userService.list();
            assertEquals(users, result);
        }
    }

    @Nested
    @DisplayName("findByIdOrElseThrow")
    class FindByIdOrElseThrow {

        @Test
        @DisplayName("shouldReturnUser_WhenUserExists")
        void shouldReturnUser_WhenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            User foundUser = userService.findByIdOrElseThrow(1L);
            assertEquals(user, foundUser);
        }

        @Test
        @DisplayName("shouldThrowNotFoundException_WhenUserDoesNotExist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> userService.findByIdOrElseThrow(1L));
        }
    }
}
