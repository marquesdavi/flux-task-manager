package com.todo.flux.module.user.service;

import java.util.List;
import java.util.UUID;

public interface UserService<Entity, Req, Res> {
    void create(Req dto);
    List<Entity> list();
    List<Res> listByBoardId(UUID boardId);
    Entity findByEmail(String email);
    Entity findByIdOrElseThrow(Long id);
    void update(Long id, Req dto);
    Entity getByID(Long id);
    void deleteAccount(Long id);
}
