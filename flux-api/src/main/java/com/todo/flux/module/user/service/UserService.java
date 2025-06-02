package com.todo.flux.module.user.service;

import java.util.List;

public interface UserService<Entity, Req> {
    void create(Req dto);
    List<Entity> list();

    Entity findByEmail(String email);
    Entity findByIdOrElseThrow(Long id);

    void update(Long id, Req dto);

    Entity getByID(Long id);

    void deleteAccount(Long id);
}
