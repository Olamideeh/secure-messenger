package com.secure.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter

public class RegisterUserRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Country region is required")
    private String region;
}
