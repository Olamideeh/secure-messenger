
package com.secure.messenger.repository;

import com.secure.messenger.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    // Find a contact relationship between two users, regardless of order
    @Query("""
        SELECT c FROM Contact c
        WHERE (c.userLow.id = :userA AND c.userHigh.id = :userB)
        OR (c.userLow.id = :userB AND c.userHigh.id = :userA)
    """)
    Optional<Contact> findContactBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    // Check whether two users are already contacts
    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Contact c
        WHERE (c.userLow.id = :userA AND c.userHigh.id = :userB)
        OR (c.userLow.id = :userB AND c.userHigh.id = :userA)
    """)
    boolean existsContactBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    // Retrieve all contact relationships belonging to a user
    @Query("""
        SELECT c FROM Contact c
        WHERE c.userLow.id = :userId
        OR c.userHigh.id = :userId
    """)
    Page<Contact> findAllContactsByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
