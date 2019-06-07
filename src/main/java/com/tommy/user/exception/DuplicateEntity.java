package com.tommy.user.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEntity extends GeneralException {

    public DuplicateEntity(String entity) {
        super();
        super.setMessage("[Duplicate entity] : " + entity);
        super.setStatus(HttpStatus.CONFLICT);
    }
}
