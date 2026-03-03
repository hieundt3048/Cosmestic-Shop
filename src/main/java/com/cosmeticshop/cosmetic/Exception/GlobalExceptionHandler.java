package com.cosmeticshop.cosmetic.Exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found", ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
        public Object handleRuntimeException(RuntimeException ex){
        logger.warn("Runtime exception", ex);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> formatFieldError(fieldError))
                .collect(Collectors.joining("; "));

        logger.warn("Validation exception: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message.isBlank() ? "Dữ liệu không hợp lệ" : message);
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();
        return defaultMessage == null || defaultMessage.isBlank()
                ? fieldError.getField() + " không hợp lệ"
                : defaultMessage;
    }

}

