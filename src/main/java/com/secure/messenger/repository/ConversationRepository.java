
package com.secure.messenger.repository;

import com.secure.messenger.entity.Conversation;
import com.secure.messenger.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Find an existing direct conversation between two users
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.type = :type
        AND (
            (c.directUserLow.id = :userA AND c.directUserHigh.id = :userB)
            OR
            (c.directUserLow.id = :userB AND c.directUserHigh.id = :userA)
        )
    """)
    Optional<Conversation> findDirectConversationBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB,
            @Param("type") ConversationType type
    );

    // Check whether a direct conversation already exists
    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Conversation c
        WHERE c.type = :type
        AND (
            (c.directUserLow.id = :userA AND c.directUserHigh.id = :userB)
            OR
            (c.directUserLow.id = :userB AND c.directUserHigh.id = :userA)
        )
    """)
    boolean existsDirectConversationBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB,
            @Param("type") ConversationType type
    );

    // Retrieve conversations belonging to a user through membership records
    @Query("""
        SELECT cm.conversation
        FROM ConversationMember cm
        WHERE cm.user.id = :userId
        AND cm.leftAt IS NULL
    """)
    Page<Conversation> findUserConversations(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
