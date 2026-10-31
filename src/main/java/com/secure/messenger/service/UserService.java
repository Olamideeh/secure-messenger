package com.secure.messenger.service;

import com.secure.messenger.entity.User;
import com.secure.messenger.exception.UserAlreadyExistsException;
import com.secure.messenger.repository.UserRepository;
import com.secure.messenger.util.PhoneNumberNormalizer;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    public UserService(UserRepository userRepository, PhoneNumberNormalizer phoneNumberNormalizer) {
        this.userRepository = userRepository;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User register(String phoneNumber, String region) {
        String normalizedNumber = phoneNumberNormalizer.normalize(phoneNumber, region);

        if  (phoneNumberExists (normalizedNumber)) {
            throw new UserAlreadyExistsException ("Phone number already exists!");
        }
            User user = new User (normalizedNumber);
        return userRepository.save(user);
    }
}
