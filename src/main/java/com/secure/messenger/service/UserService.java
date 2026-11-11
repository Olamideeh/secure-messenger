
package com.secure.messenger.service;

import com.secure.messenger.entity.User;
import com.secure.messenger.exception.UserAlreadyExistsException;
import com.secure.messenger.repository.UserRepository;
import com.secure.messenger.util.PhoneNumberNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final OtpVerificationService otpVerificationService;

    public UserService(
            UserRepository userRepository,
            PhoneNumberNormalizer phoneNumberNormalizer,
            OtpVerificationService otpVerificationService) {
        this.userRepository = userRepository;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.otpVerificationService = otpVerificationService;
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    public User register(String phoneNumber, String region) {
        String normalizedNumber =
                phoneNumberNormalizer.normalize(phoneNumber, region);

        if (phoneNumberExists(normalizedNumber)) {
            throw new UserAlreadyExistsException(
                    "Phone number already exists!"
            );
        }

        User user = new User(normalizedNumber);

        User savedUser = userRepository.save(user);

        otpVerificationService.generateAndSaveOtp(savedUser.getId());

        return savedUser;
    }
}
