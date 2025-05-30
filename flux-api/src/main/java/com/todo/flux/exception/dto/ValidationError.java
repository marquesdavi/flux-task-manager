package com.todo.flux.exception.dto;

import lombok.Data;

@Data
public class ValidationError {
    private String field;
    private String message;
    private Object invalidValue;

    public ValidationError(String field, String message, Object invalidValue) {
        this.field = field;
        this.message = message;
        this.invalidValue = invalidValue;
    }
}

