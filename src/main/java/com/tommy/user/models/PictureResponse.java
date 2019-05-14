package com.tommy.user.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PictureResponse {
    String picture;

    public PictureResponse(String picture) {
        this.picture = picture;
    }
}