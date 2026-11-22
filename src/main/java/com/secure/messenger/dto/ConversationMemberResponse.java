package com.secure.messenger.dto;

import com.secure.messenger.enums.ConversationMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;
@Getter
@AllArgsConstructor
public class ConversationMemberResponse {

    private UUID id;
    private UUID userId;
    private ConversationMemberRole role;
    private OffsetDateTime joinedAt;
    private OffsetDateTime leftAt;
}
