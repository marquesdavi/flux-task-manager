// src/main/java/com/todo/flux/module/card/service/impl/CardServiceImpl.java
package com.todo.flux.module.card.service.impl;

import com.todo.flux.exception.NotFoundException;
import com.todo.flux.module.auth.dto.LoginRequest;
import com.todo.flux.module.auth.dto.TokenResponse;
import com.todo.flux.module.auth.service.AuthenticationService;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.board.service.BoardService;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.card.entity.Card;
import com.todo.flux.module.card.repository.CardRepository;
import com.todo.flux.module.card.service.CardService;
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
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final BoardService boardService;
    private final AuthenticationService<User, LoginRequest, TokenResponse> authService;

    @Override
    @Transactional
    public CardResponse create(UUID boardId, CardCreateRequest dto) {
        User currentUser = authService.getAuthenticated();
        Board board = boardService.findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        log.info("Creating card '{}' on board id={} for user id={}", dto.title(), boardId, currentUser.getId());
        Card card = Card.fromRequest(dto, board);
        Card saved = cardRepository.save(card);
        return saved.toResponse();
    }

    @Override
    public List<CardResponse> listByBoard(UUID boardId) {
        User currentUser = authService.getAuthenticated();
        Board board = boardService.findById(boardId);

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Board not found for this user");
        }

        return cardRepository.findByBoardId(boardId)
                .stream()
                .map(Card::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CardResponse getById(UUID cardId) {
        User currentUser = authService.getAuthenticated();
        Card card = findById(cardId);

        if (!card.getBoard().getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Card not found for this user");
        }

        return card.toResponse();
    }

    @Override
    @Transactional
    public CardResponse update(UUID cardId, CardUpdateRequest dto) {
        User currentUser = authService.getAuthenticated();
        Card card = findById(cardId);

        if (!card.getBoard().getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Card not found for this user");
        }

        log.info("Updating card id={} for user id={}", cardId, currentUser.getId());
        card.applyUpdate(dto);
        Card updated = cardRepository.save(card);
        return updated.toResponse();
    }

    @Override
    @Transactional
    public void delete(UUID cardId) {
        User currentUser = authService.getAuthenticated();
        Card card = findById(cardId);

        if (!card.getBoard().getOwner().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Card not found for this user");
        }

        log.info("Deleting card id={} for user id={}", cardId, currentUser.getId());
        cardRepository.delete(card);
    }

    @Override
    public Card findById(UUID uuid) {
        return cardRepository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }
}
