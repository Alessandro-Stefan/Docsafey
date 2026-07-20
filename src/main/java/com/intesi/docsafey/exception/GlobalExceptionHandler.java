package com.intesi.docsafey.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.BindErrorUtils;

import com.intesi.docsafey.exception.richiestaCons.RichiestaAlreadyExistsException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaInvalidStatusException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RichiestaNotFoundException.class)
    public ProblemDetail handleNotFound(RichiestaNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Richiesta not found");

        return problem;
    }

    @ExceptionHandler(RichiestaAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(RichiestaAlreadyExistsException ex) {
        log.warn("Already exists: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Richiesta already exists");
        
        return problem;
    }

    @ExceptionHandler(RichiestaInvalidStatusException.class)
    public ProblemDetail handleInvalidStatus(RichiestaInvalidStatusException ex) {
        log.warn("Invalid status: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Status Richiesta invalid");

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Invalid or malformed payload: {}", ex.getMessage());
        String detail = BindErrorUtils.resolveAndJoin(ex.getAllErrors());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid request payload");

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body contains an invalid value");
        problem.setDetail("Invalid request body");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Error unhandled", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occured");
        problem.setTitle("Internal error");

        return problem;
    }
}
