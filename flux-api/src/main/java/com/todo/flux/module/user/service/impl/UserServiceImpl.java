package com.todo.flux.module.user.service.impl;

import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.entity.RoleEnum;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.exception.AlreadyExistsException;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.user.repository.UserRepository;
import com.todo.flux.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService<User, RegisterRequest> {
    private final UserRepository userRepository;

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

    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("User with email {} already exists!", email);
            throw new AlreadyExistsException("User already exists");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public List<User> list() {
        return userRepository.findAll();
    }

    @Override
    public User findByIdOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }
}
