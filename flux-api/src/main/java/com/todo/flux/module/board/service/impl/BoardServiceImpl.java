package com.todo.flux.module.board.service.impl;

import com.todo.flux.config.resilience.Resilient;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
import com.todo.flux.module.board.dto.CollaboratorRequest;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.repository.BoardRepository;
import com.todo.flux.module.board.service.BoardService;
import com.todo.flux.module.user.dto.RegisterRequest;
import com.todo.flux.module.user.dto.UserSummary;
import com.todo.flux.module.user.entity.User;
import com.todo.flux.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final AuthenticationService<User, LoginRequest, TokenResponse> authService;
    private final UserService<User, RegisterRequest, UserSummary> userService;

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public BoardResponse create(BoardCreateRequest dto) {
        User currentUser = authService.getAuthenticated();
        log.info("Creating board '{}' for user id={}", dto.title(), currentUser.getId());

        Board board = Board.builder()
                .title(dto.title())
                .owner(currentUser)
                .build();

        Board saved = boardRepository.save(board);
        return saved.toResponse();
    }

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public List<BoardResponse> listAllForUser() {
        User currentUser = authService.getAuthenticated();
        log.info("Listing boards for user id={}", currentUser.getId());

        List<Board> owned = boardRepository.findByOwnerId(currentUser.getId());
        List<Board> collaborated = boardRepository.findDistinctByCollaboratorsId(currentUser.getId());
        return Stream.concat(owned.stream(), collaborated.stream())
                .map(Board::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public BoardResponse getById(UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())
                && board.getCollaborators().stream().noneMatch(u -> u.getId().equals(currentUser.getId()))) {
            throw new NotFoundException("Board not found for this user");
        }

        return board.toResponse();
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public BoardResponse update(UUID boardId, BoardUpdateRequest dto) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        log.info("Updating board id={} title to '{}' for user id={}", boardId, dto.title(), currentUser.getId());
        board.setTitle(dto.title());
        Board updated = boardRepository.save(board);
        return updated.toResponse();
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public void delete(UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        log.info("Deleting board id={} for user id={}", boardId, currentUser.getId());
        boardRepository.delete(board);
    }

    @Override
    public Board findById(UUID uuid) {
        return boardRepository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("Board not found"));
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public BoardResponse addCollaborator(UUID boardId, CollaboratorRequest dto) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        User toInvite = userService.findByEmail(dto.email());

        if (toInvite.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Owner já tem acesso");
        }

        if (board.getCollaborators().contains(toInvite)) {
            throw new IllegalArgumentException("Usuário já é colaborador");
        }

        board.getCollaborators().add(toInvite);
        Board updated = boardRepository.save(board);
        return updated.toResponse();
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public BoardResponse removeCollaborator(UUID boardId, CollaboratorRequest dto) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        User toRemove = userService.findByEmail(dto.email());
        if (!board.getCollaborators().contains(toRemove)) {
            throw new NotFoundException("Usuário não é colaborador desta board");
        }

        board.getCollaborators().remove(toRemove);
        Board updated = boardRepository.save(board);
        return updated.toResponse();
    }
}
