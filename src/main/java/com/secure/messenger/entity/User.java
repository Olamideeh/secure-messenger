package com.secure.messenger.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Data
@Entity
@Table ( name = "users")
public class User {

    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phoneVerified = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column( name = "phone_number", nullable = false,unique = true,length = 20)
    private String phoneNumber;

    @Column( name = "phone_verified",nullable = false)
    private boolean phoneVerified;

    @Column( name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    protected User() {
    }

}
