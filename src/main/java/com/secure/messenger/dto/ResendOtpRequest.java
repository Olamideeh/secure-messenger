package com.secure.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResendOtpRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String region;

}
