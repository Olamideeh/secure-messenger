package com.secure.messenger.dto;

import com.secure.messenger.enums.ContactRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;
@Getter
@AllArgsConstructor
public class ContactRequestResponse {

    private UUID id;
    private UUID senderId;
    private UUID recipientId;
    private ContactRequestStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime respondedAt;

}
