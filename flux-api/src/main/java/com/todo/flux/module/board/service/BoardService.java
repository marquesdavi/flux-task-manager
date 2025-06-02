package com.todo.flux.module.board.service;

import com.todo.flux.module.board.dto.BoardCreateRequest;
import com.todo.flux.module.board.dto.BoardResponse;
import com.todo.flux.module.board.dto.BoardUpdateRequest;
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
    BoardResponse create(String userId, BoardCreateRequest dto);
    List<BoardResponse> listAllForUser(String userId);
    BoardResponse getById(String userId, UUID boardId);
    BoardResponse update(String userId, UUID boardId, BoardUpdateRequest dto);
    void delete(String userId, UUID boardId);
}
