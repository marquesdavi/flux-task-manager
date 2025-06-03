package com.todo.flux.module.shared;

import java.util.List;

public interface CrudService<
        Entity,
        CreateDto,
        UpdateDto,
        ResponseDto,
        ID
        > {
    default ResponseDto create(CreateDto dto) {
        return null;
    }

    default List<ResponseDto> listAllForUser() {
        return null;
    }

    ResponseDto getById(ID id);

    ResponseDto update(ID id, UpdateDto dto);

    void delete(ID id);

    Entity findById(ID id);
}
