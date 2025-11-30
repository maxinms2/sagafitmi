package com.sagafitmi.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError err = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler({ BadRequestException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Failed", msg, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    @ExceptionHandler(UserCreateException.class)
    public ResponseEntity<Map<String, String>> handleUserCreation(UserCreateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserUpdateException.class)
    public ResponseEntity<Map<String, String>> handleUserUpdate(UserUpdateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }


    @ExceptionHandler(UserDeleteException.class)
    public ResponseEntity<Map<String, String>> handleUserDelete(UserDeleteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ProductCreateException.class)
    public ResponseEntity<Map<String, String>> handleProductCreation(ProductCreateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }
}