package com.tommy.user.controllers;


import com.tommy.user.exception.DuplicateEntity;
import com.tommy.user.exception.NotExistingEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {


    @ExceptionHandler(NotExistingEntity.class)
    public final ResponseEntity<?> entityDoesNotExist(NotExistingEntity ex){
        return ResponseEntity.status(ex.getStatus()).body(ex);
    }

    @ExceptionHandler(DuplicateEntity.class)
    public final ResponseEntity<?> duplicateEntity(DuplicateEntity ex){
        return ResponseEntity.status(ex.getStatus()).body(ex);
    }
}
