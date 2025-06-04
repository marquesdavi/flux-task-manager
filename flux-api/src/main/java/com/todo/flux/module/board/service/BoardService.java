package com.todo.flux.module.board.service;

import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
import com.todo.flux.module.board.dto.CollaboratorRequest;
import com.todo.flux.module.board.entity.Board;
import com.todo.flux.module.shared.CrudService;

import java.util.List;
import java.util.UUID;

public interface BoardService extends CrudService<
        Board,
        BoardCreateRequest,
        BoardUpdateRequest,
        BoardResponse,
        UUID
        > {
    BoardResponse create(BoardCreateRequest dto);
    List<BoardResponse> listAllForUser();
    BoardResponse getById(UUID boardId);
    BoardResponse update(UUID boardId, BoardUpdateRequest dto);
    void delete(UUID boardId);
    BoardResponse addCollaborator(UUID boardId, CollaboratorRequest dto);
    BoardResponse removeCollaborator(UUID boardId, CollaboratorRequest dto);
}
