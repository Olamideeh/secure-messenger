package com.secure.messenger.repository;

import com.secure.messenger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByPhoneNumber (String phoneNumber);
}
