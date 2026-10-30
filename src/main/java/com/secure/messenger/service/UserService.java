package com.secure.messenger.service;

import com.secure.messenger.entity.User;
import com.secure.messenger.exception.UserAlreadyExistsException;
import com.secure.messenger.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User register(String phoneNumber ) {
        if  (phoneNumberExists(phoneNumber)) {
            throw new UserAlreadyExistsException ("Phone number already exists!");
        }
            User user = new User (phoneNumber);
        return userRepository.save(user);
    }
}
