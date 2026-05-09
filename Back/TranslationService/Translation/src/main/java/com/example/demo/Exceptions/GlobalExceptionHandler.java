// Exceptions/GlobalExceptionHandler.java
package com.example.demo.Exceptions;

import com.example.demo.DTO.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(404)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoDataFound(NoDataFoundException ex) {
        return ResponseEntity.status(404).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(404)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(InvalidPageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPage(InvalidPageException ex) {
        return ResponseEntity.status(400).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(400)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(409)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ex.printStackTrace(); // ADD THIS LINE
        return ResponseEntity.status(500).body(
                ErrorResponse.builder()
                        .message("Something went wrong")
                        .status(500)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

}