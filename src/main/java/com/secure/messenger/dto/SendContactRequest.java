package com.secure.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendContactRequest {

    @NotBlank(message = "input correct phone Number")
    private String recipientPhoneNumber;
}
