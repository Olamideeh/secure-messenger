package com.secure.messenger.dto;

import com.secure.messenger.entity.User;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class RegisterUserResponse {

    private UUID id;
    private String phoneNumber;
    private boolean phoneVerified;
    private OffsetDateTime createdAt;

    public RegisterUserResponse() {
    }

    public RegisterUserResponse(User user) {
        this.id = user.getId();
        this.phoneNumber = user.getPhoneNumber();
        this.phoneVerified = user.isPhoneVerified();
        this.createdAt = user.getCreatedAt();
    }
}