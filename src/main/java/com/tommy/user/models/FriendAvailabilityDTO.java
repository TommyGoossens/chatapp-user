package com.tommy.user.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendAvailabilityDTO {
    private boolean isAvailable;
    private String message;

    public FriendAvailabilityDTO(boolean isAvailable, String message) {
        this.isAvailable = isAvailable;
        this.message = message;
    }
}
