
package com.secure.messenger.service;

import com.secure.messenger.entity.User;
import com.secure.messenger.exception.InvalidPhoneNumberException;
import com.secure.messenger.repository.UserRepository;
import com.secure.messenger.util.PhoneNumberNormalizer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationService otpVerificationService;
    private final PhoneNumberNormalizer phoneNumberNormalizer;

    public AuthService(
            UserRepository userRepository,
            OtpVerificationService otpVerificationService,
            PhoneNumberNormalizer phoneNumberNormalizer) {
        this.userRepository = userRepository;
        this.otpVerificationService = otpVerificationService;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
    }

    @Transactional
    public boolean verifyPhoneNumber(
            String phoneNumber,
            String region,
            String otp) {

        // Validate phone number
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException(
                    "Invalid phone number"
            );
        }

        // Normalize phone number
        String normalizedNumber =
                phoneNumberNormalizer.normalize(phoneNumber, region);

        // Find user
        Optional<User> userOptional =
                userRepository.findByPhoneNumber(normalizedNumber);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Check whether phone number is already verified
        if (user.isPhoneVerified()) {
            return true;
        }

        // Verify submitted OTP
        boolean verified = otpVerificationService.verifyOtp(
                user.getId(),
                otp
        );

        if (!verified) {
            return false;
        }

        // Mark phone number as verified
        user.setPhoneVerified(true);

        userRepository.save(user);

        return true;
    }
}
