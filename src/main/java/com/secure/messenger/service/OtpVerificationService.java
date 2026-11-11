
package com.secure.messenger.service;

import com.secure.messenger.entity.OtpVerification;
import com.secure.messenger.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class OtpVerificationService {

    private final OtpVerificationRepository otpVerificationRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 3;
    private static final int MAX_ATTEMPTS = 3;

    @Value("${security.otp.hmac-secret}")
    private String otpHmacSecret;

    public OtpVerificationService(
            OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
    }

    // Generate a six-digit OTP
    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    // Generate OTP, save its hash and return the plain OTP for delivery
    @Transactional
    public String generateAndSaveOtp(UUID userId) {
        String otp = generateOtp();
        saveOtp(userId, otp);
        return otp;
    }

    // Hash OTP using HMAC-SHA256
    private String hashOtp(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    otpHmacSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(
                    otp.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to hash OTP", e);
        }
    }

    // Calculate OTP expiry time
    private Instant calculateExpiryTime() {
        return Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
    }

    // Save a new OTP or replace an existing one
    @Transactional
    public void saveOtp(UUID userId, String otp) {

        OtpVerification existingOtp = otpVerificationRepository
                .findByUserId(userId)
                .orElse(null);

        if (existingOtp == null) {
            existingOtp = new OtpVerification();
        }

        existingOtp.setOtpHash(hashOtp(otp));
        existingOtp.setUserId(userId);
        existingOtp.setExpiresAt(calculateExpiryTime());
        existingOtp.setAttempts(0);
        existingOtp.setUsedAt(null);

        otpVerificationRepository.save(existingOtp);
    }

    // Verify a submitted OTP
    @Transactional
    public boolean verifyOtp(UUID userId, String submittedOtp) {

        OtpVerification otpVerification = otpVerificationRepository
                .findByUserId(userId)
                .orElse(null);

        if (otpVerification == null) {
            return false;
        }

        if (otpVerification.getUsedAt() != null) {
            return false;
        }

        if (otpVerification.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        if (otpVerification.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        otpVerification.setAttempts(otpVerification.getAttempts() + 1);

        String submittedOtpHash = hashOtp(submittedOtp);

        if (!java.security.MessageDigest.isEqual(
                HexFormat.of().parseHex(otpVerification.getOtpHash()),
                HexFormat.of().parseHex(submittedOtpHash))) {

            otpVerificationRepository.save(otpVerification);
            return false;
        }

        otpVerification.setUsedAt(Instant.now());
        otpVerificationRepository.save(otpVerification);

        return true;
    }
}
