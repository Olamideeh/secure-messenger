package com.secure.messenger.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
public class ErrorResponse {

    public ErrorResponse(int status, String message, OffsetDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    private int status;
    private String message;
    private OffsetDateTime timestamp;

}
