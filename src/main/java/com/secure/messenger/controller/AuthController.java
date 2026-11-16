package com.secure.messenger.controller;

import com.secure.messenger.dto.VerifyOtpRequest;
import com.secure.messenger.dto.VerifyOtpResponse;
import com.secure.messenger.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController{

    private AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest request){
        boolean verified = authService.verifyPhoneNumber(
                request.getPhoneNumber(),
                request.getRegion(),
                request.getOtp()
        );

        VerifyOtpResponse response = new VerifyOtpResponse();
        response.setVerified(verified);
        response.setMessage(verified ? "Phone number successfully verified" : "invalid or expired Otp");

        return ResponseEntity.ok(response);
    }
}