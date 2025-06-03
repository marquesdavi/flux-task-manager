// src/main/java/com/todo/flux/module/card/service/CardService.java
package com.todo.flux.module.card.service;

import com.todo.flux.module.card.dto.CardCreateRequest;
import com.todo.flux.module.card.dto.CardResponse;
import com.todo.flux.module.card.dto.CardUpdateRequest;
import com.todo.flux.module.card.entity.Card;
import com.todo.flux.module.shared.CrudService;

import java.util.List;
import java.util.UUID;

public interface CardService extends CrudService<
                                            Card,
                                            CardCreateRequest,
                                            CardUpdateRequest,
                                            CardResponse,
                                            UUID
                                            > {

    List<CardResponse> listByBoard(UUID boardId);

    CardResponse create(UUID boardId, CardCreateRequest dto);
}
