package com.ticketproject.webapp.exceptions;

import com.ticketproject.webapp.dtos.responses.ErrorResponse;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * GlobalExceptionHandler is a centralized exception handler for all
 * controllers in the application. It catches specific exceptions and
 * returns structured error responses with appropriate HTTP status codes
 * instead of letting the server return a generic 500 Internal Server Error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    /**
     * Handles EventHostEmailAlreadyExistsException by returning a
     * 409 Conflict response.
     *
     * @param ex      the exception that was thrown
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing the error response and a 409 status
     */
    @ExceptionHandler(EventHostEmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists
    (
        EventHostEmailAlreadyExistsException ex,
        HttpServletRequest request
    )
    {
        ErrorResponse errorResponse = new ErrorResponse
        (
            Instant.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles EventHostUnderageException by returning a
     * 400 Bad Request response.
     *
     * @param ex      the exception that was thrown
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing the error response and a 400 status
     */
    @ExceptionHandler(EventHostUnderageException.class)
    public ResponseEntity<ErrorResponse> handleUnderage
    (
        EventHostUnderageException ex,
        HttpServletRequest request
    )
    {
        ErrorResponse errorResponse = new ErrorResponse
        (
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentNotValidException (thrown when @Valid
     * validation fails on a request body) by returning a
     * 400 Bad Request response with all field-level validation errors
     * concatenated into the message.
     *
     * @param ex      the exception that was thrown
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing the error response and a 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors
    (
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    )
    {
        String validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            validationErrors,
            request.getRequestURI()
        );
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}