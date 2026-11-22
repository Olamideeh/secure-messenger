
package com.secure.messenger.repository;

import com.secure.messenger.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository
        extends JpaRepository<ConversationMember, UUID> {

    // Find a user's membership in a conversation
    Optional<ConversationMember> findByConversation_IdAndUser_Id(
            UUID conversationId,
            UUID userId
    );

    // Check whether a user has a membership record
    boolean existsByConversation_IdAndUser_Id(
            UUID conversationId,
            UUID userId
    );

    // Check whether a user has active membership
    boolean existsByConversation_IdAndUser_IdAndLeftAtIsNull(
            UUID conversationId,
            UUID userId
    );

    // Retrieve all active members of a conversation
    List<ConversationMember> findByConversation_IdAndLeftAtIsNull(
            UUID conversationId
    );
}
