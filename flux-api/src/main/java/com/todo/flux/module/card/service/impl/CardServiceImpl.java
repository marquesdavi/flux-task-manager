package com.todo.flux.module.card.service.impl;

import com.todo.flux.config.resilience.Resilient;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.service.BoardService;
import com.todo.flux.module.card.dto.CardAssignRequest;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.card.entity.Card;
import com.todo.flux.module.card.repository.CardRepository;
import com.todo.flux.module.card.service.CardService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final BoardService boardService;
    private final AuthenticationService<User, LoginRequest, TokenResponse> authService;
    private final UserService<User, RegisterRequest, UserSummary> userService;

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public CardResponse create(UUID boardId, CardCreateRequest dto) {
        Board board = getOwnBoard(boardId);

        log.info("Creating card '{}' on board id={}", dto.title(), boardId);
        Card card = Card.fromRequest(dto, board);
        Card saved = cardRepository.save(card);
        return saved.toResponse();
    }

    private Board getOwnBoard(UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = boardService.findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId()) &&
                board.getCollaborators().stream().noneMatch(u -> u.getId().equals(currentUser.getId()))) {
            throw new NotFoundException("Board not found for this user");
        }

        return board;
    }

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public List<CardResponse> listByBoard(UUID boardId) {
        getOwnBoard(boardId);

        return cardRepository.findByBoardId(boardId)
                .stream()
                .map(Card::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public CardResponse getById(UUID cardId) {
        Card card = getOwnCard(cardId);

        return card.toResponse();
    }

    private Card getOwnCard(UUID cardId) {
        User currentUser = authService.getAuthenticated();
        Card card = findById(cardId);

        if (!card.getBoard().getOwner().getId().equals(currentUser.getId())
                && card.getBoard().getCollaborators().stream().noneMatch(u -> u.getId().equals(currentUser.getId()))) {
            throw new NotFoundException("Card not found for this user");
        }

        return card;
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public CardResponse update(UUID cardId, CardUpdateRequest dto) {
        Card card = getOwnCard(cardId);

        log.info("Updating card id={}", cardId);
        card.applyUpdate(dto);
        Card updated = cardRepository.save(card);
        return updated.toResponse();
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public void delete(UUID cardId) {
        Card card = getOwnCard(cardId);

        log.info("Deleting card id={}", cardId);
        cardRepository.delete(card);
    }

    @Override
    public Card findById(UUID uuid) {
        return cardRepository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    @Override
    @Transactional
    @Resilient(rateLimiter = "RateLimiter", circuitBreaker = "CircuitBreaker")
    public CardResponse assignCard(UUID cardId, CardAssignRequest dto) {
        Card card = getOwnCard(cardId);

        User toAssign = userService.findByEmail(dto.email());

        boolean isOwner = card.getBoard().getOwner().getId().equals(toAssign.getId());
        boolean isCollaborator = card.getBoard().getCollaborators().stream()
                .anyMatch(u -> u.getId().equals(toAssign.getId()));
        if (!isOwner && !isCollaborator) {
            throw new IllegalArgumentException("Usuário não tem acesso à board");
        }

        card.assignTo(toAssign);
        Card updated = cardRepository.save(card);
        return updated.toResponse();
    }
}
