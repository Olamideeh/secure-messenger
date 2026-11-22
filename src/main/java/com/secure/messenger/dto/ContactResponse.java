package com.secure.messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ContactResponse {

    private UUID contactId;
    private UUID userId;
    private String phoneNumber;
    private OffsetDateTime createdAt;
}
