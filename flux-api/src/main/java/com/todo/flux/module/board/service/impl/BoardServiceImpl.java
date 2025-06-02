package com.todo.flux.module.board.service.impl;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final AuthenticationService<User, ?, ?> authService;

    @Override
    @Transactional
    public BoardResponse create(String userId, BoardCreateRequest dto) {
        User currentUser = authService.getAuthenticated();
        log.info("Creating board '{}' for user id={}", dto.title(), currentUser.getId());

        Board board = Board.builder()
                .title(dto.title())
                .owner(currentUser)
                .build();

        Board saved = boardRepository.save(board);
        return mapToResponse(saved);
    }

    @Override
    public List<BoardResponse> listAllForUser(String userId) {
        User currentUser = authService.getAuthenticated();
        log.info("Listing boards for user id={}", currentUser.getId());

        return boardRepository.findByOwnerId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BoardResponse getById(String userId, UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        return mapToResponse(board);
    }

    @Override
    @Transactional
    public BoardResponse update(String userId, UUID boardId, BoardUpdateRequest dto) {
        User currentUser = authService.getAuthenticated();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        log.info("Updating board id={} title to '{}' for user id={}", boardId, dto.title(), currentUser.getId());
        board.setTitle(dto.title());
        Board updated = boardRepository.save(board);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(String userId, UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        log.info("Deleting board id={} for user id={}", boardId, currentUser.getId());
        boardRepository.delete(board);
    }

    private BoardResponse mapToResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getOwner().getId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
