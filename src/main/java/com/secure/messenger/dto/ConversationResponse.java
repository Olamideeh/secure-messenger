package com.secure.messenger.dto;

import com.secure.messenger.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;
@AllArgsConstructor
@Getter
public class ConversationResponse {

    private UUID id;
    private ConversationType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
