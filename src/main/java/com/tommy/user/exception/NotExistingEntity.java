package com.tommy.user.exception;

import org.springframework.http.HttpStatus;

public class NotExistingEntity extends GeneralException {

    public NotExistingEntity(String message) {
        super();
        super.setMessage("[Email does not exist in database] : " + message);
        super.setStatus(HttpStatus.NOT_FOUND);
    }
}
