package com.todo.flux.exception.handler;

import com.todo.flux.exception.AlreadyExistsException;
import com.todo.flux.exception.GenericException;
import com.todo.flux.exception.NotFoundException;
import com.todo.flux.exception.dto.ErrorResponse;
import com.todo.flux.exception.dto.ValidationError;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GenericExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        log.error("NotFoundException: {}", exception.getMessage());
        return buildErrorResponse(exception.getMessage(), HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(AlreadyExistsException exception) {
        log.error("AlreadyExistsException: {}", exception.getMessage());
        return buildErrorResponse(exception.getMessage(), HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("IllegalArgumentException: {}", exception.getMessage());
        return buildErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception exception) {
        log.error("Uncaught exception: ", exception);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ErrorResponse> handleGenericException(GenericException exception) {
        log.error("GenericException: {}", exception.getMessage());
        return buildErrorResponse(exception.getMessage(), HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<ValidationError> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(constraintViolation -> {
            String fieldName = constraintViolation.getPropertyPath().toString();
            String message = constraintViolation.getMessage();
            Object invalidValue = constraintViolation.getInvalidValue();
            errors.add(new ValidationError(fieldName, message, invalidValue));
        });
        log.error("ConstraintViolationException: {}", errors);
        return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentNotValidException exception) {
        List<ValidationError> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error -> errors.add(
                        new ValidationError(
                                error.getField(),
                                error.getDefaultMessage(),
                                error.getRejectedValue()
                        )));
        log.error("MethodArgumentNotValidException: {}", errors);
        return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<ErrorResponse> handleRateLimiterException(RequestNotPermitted ex) {
        log.error("RateLimiter exception: {}", ex.getMessage());
        return buildErrorResponse("Too many requests - please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, List<ValidationError> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status, errors));
    }
}
