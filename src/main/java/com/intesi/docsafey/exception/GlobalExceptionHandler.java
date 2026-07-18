package com.intesi.docsafey.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.intesi.docsafey.exception.richiestaCons.RichiestaAlreadyExistsException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaInvalidStatusException;
import com.intesi.docsafey.exception.richiestaCons.RichiestaNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(RichiestaNotFoundException.class)
    public ProblemDetail handleNotFound(RichiestaNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Richiesta not found");

        return problem;
    }

    @ExceptionHandler(RichiestaAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(RichiestaAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Richiesta already exists");
        
        return problem;
    }

    @ExceptionHandler(RichiestaInvalidStatusException.class)
    public ProblemDetail handleInvalidStatus(RichiestaInvalidStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Status Richiesta invalid");

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
