
package com.secure.messenger.service;

import com.secure.messenger.entity.User;
import com.secure.messenger.exception.InvalidPhoneNumberException;
import com.secure.messenger.repository.UserRepository;
import com.secure.messenger.util.PhoneNumberNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationService otpVerificationService;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final SmsProvider smsProvider;

    public AuthService(
            UserRepository userRepository,
            OtpVerificationService otpVerificationService,
            PhoneNumberNormalizer phoneNumberNormalizer,
            SmsProvider smsProvider
    ) {
        this.userRepository = userRepository;
        this.otpVerificationService = otpVerificationService;
        this.phoneNumberNormalizer = phoneNumberNormalizer;
        this.smsProvider = smsProvider;
    }

    @Transactional
    public boolean verifyPhoneNumber(
            String phoneNumber,
            String region,
            String otp
    ) {
        return verifyAndLoadUser(phoneNumber, region, otp) != null;
    }

    @Transactional
    public User verifyPhoneNumberAndGetUser(
            String phoneNumber,
            String region,
            String otp
    ) {
        return verifyAndLoadUser(phoneNumber, region, otp);
    }

    private User verifyAndLoadUser(
            String phoneNumber,
            String region,
            String otp
    ) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }

        String normalizedNumber =
                phoneNumberNormalizer.normalize(phoneNumber, region);

        Optional<User> userOptional =
                userRepository.findByPhoneNumber(normalizedNumber);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        boolean verified = otpVerificationService.verifyOtp(
                user.getId(),
                otp
        );

        if (!verified) {
            return null;
        }

        if (!user.isPhoneVerified()) {
            user.setPhoneVerified(true);
            userRepository.save(user);
        }

        return user;
    }

    @Transactional
    public boolean resendOtp(String phoneNumber, String region) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Invalid phone number");
        }

        String normalizedNumber =
                phoneNumberNormalizer.normalize(phoneNumber, region);

        Optional<User> userOptional =
                userRepository.findByPhoneNumber(normalizedNumber);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (!otpVerificationService.canResendOtp(user.getId())) {
            return false;
        }

        String otp =
                otpVerificationService.generateAndSaveOtp(user.getId());

        String message =
                "Your secure messenger verification code is " + otp;

        smsProvider.sendSms(user.getPhoneNumber(), message);

        return true;
    }
}
