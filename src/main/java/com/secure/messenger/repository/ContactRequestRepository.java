
package com.secure.messenger.repository;

import com.secure.messenger.entity.ContactRequest;
import com.secure.messenger.enums.ContactRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, UUID> {

    // Incoming contact requests
    Page<ContactRequest> findByRecipient_Id(
            UUID recipientId,
            Pageable pageable
    );

    // Outgoing contact requests
    Page<ContactRequest> findBySender_Id(
            UUID senderId,
            Pageable pageable
    );

    // Find a pending request between two users, regardless of direction
    @Query("""
        SELECT cr FROM ContactRequest cr
        WHERE cr.status = :status
        AND (
            (cr.sender.id = :userA AND cr.recipient.id = :userB)
            OR
            (cr.sender.id = :userB AND cr.recipient.id = :userA)
        )
    """)
    Optional<ContactRequest> findPendingBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB,
            @Param("status") ContactRequestStatus status
    );

    // Check whether a pending request exists between two users
    @Query("""
        SELECT CASE WHEN COUNT(cr) > 0 THEN TRUE ELSE FALSE END
        FROM ContactRequest cr
        WHERE cr.status = :status
        AND (
            (cr.sender.id = :userA AND cr.recipient.id = :userB)
            OR
            (cr.sender.id = :userB AND cr.recipient.id = :userA)
        )
    """)
    boolean existsPendingBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB,
            @Param("status") ContactRequestStatus status
    );
}
