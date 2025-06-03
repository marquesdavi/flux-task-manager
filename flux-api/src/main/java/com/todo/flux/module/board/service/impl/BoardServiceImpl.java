package com.todo.flux.module.board.service.impl;

import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.repository.BoardRepository;
import com.todo.flux.module.board.service.BoardService;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final AuthenticationService<User, LoginRequest, TokenResponse> authService;

    @Override
    @Transactional
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
    public List<BoardResponse> listAllForUser() {
        User currentUser = authService.getAuthenticated();
        log.info("Listing boards for user id={}", currentUser.getId());

        return boardRepository.findByOwnerId(currentUser.getId()).stream()
                .map(Board::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BoardResponse getById(UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        return board.toResponse();
    }

    @Override
    @Transactional
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
}
