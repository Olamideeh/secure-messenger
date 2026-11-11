package com.secure.messenger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpResponse {

    private boolean verified;
    private String message;

}
