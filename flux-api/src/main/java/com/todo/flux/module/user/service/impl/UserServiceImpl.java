package com.todo.flux.module.user.service.impl;

import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.entity.RoleEnum;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.exception.AlreadyExistsException;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.user.repository.UserRepository;
import com.todo.flux.module.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService<User, RegisterRequest> {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; // injetar via @Bean no SecurityConfig, por exemplo

    @Override
    @Transactional
    public void create(RegisterRequest dto) {
        log.info("Creating new user with email: {}", dto.email());
        existsByEmail(dto.email());

        User user = User.fromRequest(dto);
        user.setRole(RoleEnum.USER);

        userRepository.save(user);
        log.info("User with email {} successfully created.", dto.email());
    }

    private void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("User with email {} already exists!", email);
            throw new AlreadyExistsException("User already exists");
        }
    }

    @Override
    public List<User> list() {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findByIdOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }

    @Override
    @Transactional
    public void update(Long id, RegisterRequest dto) {
        log.info("Updating user id={} with email: {}", id, dto.email());
        User existing = findByIdOrElseThrow(id);

        // Se o e-mail foi alterado, verificar duplicidade
        if (!existing.getEmail().equals(dto.email())) {
            existsByEmail(dto.email());
            existing.setEmail(dto.email());
        }

        // Atualizar nome
        existing.setFirstName(dto.firstName());
        existing.setLastName(dto.lastName());

        // Atualizar senha (sempre re-hashear)
        String encodedPassword = passwordEncoder.encode(dto.password());
        existing.setPassword(encodedPassword);

        userRepository.save(existing);
        log.info("User id={} successfully updated.", id);
    }

    @Override
    public User getByID(Long id) {
        log.info("Fetching user by id={}", id);
        return findByIdOrElseThrow(id);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting user id={}", id);
        User existing = findByIdOrElseThrow(id);
        userRepository.delete(existing);
        log.info("User id={} successfully deleted.", id);
    }
}
