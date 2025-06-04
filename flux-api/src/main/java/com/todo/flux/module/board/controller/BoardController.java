package com.todo.flux.module.board.controller;

import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
import com.todo.flux.module.board.dto.CollaboratorRequest;
import com.todo.flux.module.board.service.BoardService;
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
@RequestMapping("/api/boards")
@Tag(name = "Boards", description = "Board management")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "Get all boards of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boards fetched"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllForUser() {
        List<BoardResponse> boards = boardService.listAllForUser();
        return ResponseEntity.ok(boards);
    }

    @Operation(summary = "Create a new board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<BoardResponse> create(
            @Valid @RequestBody BoardCreateRequest dto) {
        BoardResponse response = boardService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a board by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board fetched"),
            @ApiResponse(responseCode = "404", description = "Board not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getById(
            @PathVariable UUID boardId) {
        BoardResponse response = boardService.getById(boardId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a board title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Board not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/{boardId}")
    public ResponseEntity<BoardResponse> update(
            @PathVariable UUID boardId,
            @Valid @RequestBody BoardUpdateRequest dto) {
        BoardResponse response = boardService.update(boardId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a board and all its cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board deleted"),
            @ApiResponse(responseCode = "404", description = "Board not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID boardId) {
        boardService.delete(boardId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Adicionar colaborador (por e-mail) a uma board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador adicionado"),
            @ApiResponse(responseCode = "404", description = "Board ou User não encontrado"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{boardId}/collaborators")
    public ResponseEntity<BoardResponse> addCollaborator(
            @PathVariable UUID boardId,
            @Valid @RequestBody CollaboratorRequest dto) {
        BoardResponse response = boardService.addCollaborator(boardId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remover colaborador (por e-mail) de uma board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaborador removido"),
            @ApiResponse(responseCode = "404", description = "Board ou User não encontrado"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{boardId}/collaborators")
    public ResponseEntity<BoardResponse> removeCollaborator(
            @PathVariable UUID boardId,
            @Valid @RequestBody CollaboratorRequest dto) {
        BoardResponse response = boardService.removeCollaborator(boardId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar e-mails dos colaboradores de uma board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colaboradores encontrados"),
            @ApiResponse(responseCode = "404", description = "Board não encontrada ou sem acesso"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{boardId}/collaborators")
    public ResponseEntity<List<String>> listCollaborators(
            @PathVariable UUID boardId
    ) {
        BoardResponse board = boardService.getById(boardId);
        return ResponseEntity.ok(board.collaboratorsEmails());
    }
}
