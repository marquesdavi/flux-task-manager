package com.todo.flux.module.auth.service;

public interface AuthenticationService<Entity, Req, Res> {
    Res authenticate(Req request);
    Res generateResponse(Entity user);
    Entity getAuthenticated();
}
