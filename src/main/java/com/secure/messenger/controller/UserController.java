package com.secure.messenger.controller;

import com.secure.messenger.dto.RegisterUserRequest;
import com.secure.messenger.dto.RegisterUserResponse;
import com.secure.messenger.entity.User;
import com.secure.messenger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<RegisterUserResponse> registerUser(
            @Valid @RequestBody RegisterUserRequest request) {

        User user = userService.register(request.getPhoneNumber());

        RegisterUserResponse response = new RegisterUserResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}