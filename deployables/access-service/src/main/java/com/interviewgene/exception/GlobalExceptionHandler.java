package com.interviewgene.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                                 .timestamp(Instant.now())
                                 .status(HttpStatus.BAD_REQUEST.value())
                                 .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                 .message(ex.getMessage())
                                 .path(request.getRequestURI())
                                 .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                       .findFirst()
                       .map(objectError -> objectError.getDefaultMessage())
                       .orElse("Validation error");

        ApiError error = ApiError.builder()
                                 .timestamp(Instant.now())
                                 .status(HttpStatus.BAD_REQUEST.value())
                                 .error("Validation error")
                                 .message(msg)
                                 .path(request.getRequestURI())
                                 .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                                 .timestamp(Instant.now())
                                 .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                 .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                 .message(ex.getMessage())
                                 .path(request.getRequestURI())
                                 .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
