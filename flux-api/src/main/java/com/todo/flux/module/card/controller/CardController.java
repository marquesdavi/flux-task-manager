package com.todo.flux.module.card.controller;

import com.todo.flux.module.card.dto.CardAssignRequest;
import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Cards", description = "Card management")
public class CardController {
    private final CardService cardService;

    @Operation(summary = "List all cards of a board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards fetched"),
            @ApiResponse(responseCode = "404", description = "Board not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/boards/{boardId}/cards")
    public ResponseEntity<List<CardResponse>> listByBoard(
            @PathVariable UUID boardId) {
        List<CardResponse> cards = cardService.listByBoard(boardId);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Create a new card in a board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created"),
            @ApiResponse(responseCode = "404", description = "Board not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/boards/{boardId}/cards")
    public ResponseEntity<CardResponse> create(
            @PathVariable UUID boardId,
            @Valid @RequestBody CardCreateRequest dto) {
        CardResponse response = cardService.create(boardId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a card by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card fetched"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CardResponse> getById(
            @PathVariable UUID cardId) {
        CardResponse response = cardService.getById(cardId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/cards/{cardId}")
    public ResponseEntity<CardResponse> update(
            @PathVariable UUID cardId,
            @Valid @RequestBody CardUpdateRequest dto) {
        CardResponse response = cardService.update(cardId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card deleted"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID cardId) {
        cardService.delete(cardId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Assign a card to an user")
    @PatchMapping("/cards/{cardId}/assign")
    public ResponseEntity<CardResponse> assign(
            @PathVariable UUID cardId,
            @Valid @RequestBody CardAssignRequest dto) {
        return ResponseEntity.ok(cardService.assignCard(cardId, dto));
    }
}
