
package com.secure.messenger.controller;

import com.secure.messenger.dto.ResendOtpRequest;
import com.secure.messenger.dto.ResendOtpResponse;
import com.secure.messenger.dto.VerifyOtpRequest;
import com.secure.messenger.dto.VerifyOtpResponse;
import com.secure.messenger.entity.User;
import com.secure.messenger.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            SecurityContextRepository securityContextRepository
    ) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        User user = authService.verifyPhoneNumberAndGetUser(
                request.getPhoneNumber(),
                request.getRegion(),
                request.getOtp()
        );

        VerifyOtpResponse response = new VerifyOtpResponse();

        if (user == null) {
            response.setVerified(false);
            response.setMessage("Invalid or expired OTP");

            return ResponseEntity.ok(response);
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getId().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        response.setVerified(true);
        response.setMessage("Phone number successfully verified");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ResendOtpResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        boolean success = authService.resendOtp(
                request.getPhoneNumber(),
                request.getRegion()
        );

        ResendOtpResponse response = new ResendOtpResponse();

        response.setSuccess(success);

        response.setMessage(
                success
                        ? "OTP sent successfully"
                        : "Unable to resend OTP. Check your details or try again later."
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public Map<String, String> currentUser(Authentication authentication) {
        return Map.of("userId", authentication.getName());
    }
}
