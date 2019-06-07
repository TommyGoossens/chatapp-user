package com.tommy.user.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public abstract class GeneralException extends RuntimeException {
    private String message;
    private HttpStatus status;
}
