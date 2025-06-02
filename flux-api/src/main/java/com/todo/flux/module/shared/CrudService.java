package com.todo.flux.module.shared;

import java.util.List;

public interface CrudService<
        Entity,
        CreateDto,
        UpdateDto,
        ResponseDto,
        ID
        > {
    default ResponseDto create(String userId, CreateDto dto) {
        return null;
    }

    List<ResponseDto> listAllForUser(String userId);
    ResponseDto getById(String userId, ID id);
    ResponseDto update(String userId, ID id, UpdateDto dto);
    void delete(String userId, ID id);
}
