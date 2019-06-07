package com.tommy.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PasswordIncorrectException extends GeneralException {
    public PasswordIncorrectException(){
        super();
        super.setMessage("[Incorrect password]");
        super.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
