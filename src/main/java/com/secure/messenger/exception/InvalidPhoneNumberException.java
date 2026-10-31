package com.secure.messenger.exception;

public class InvalidPhoneNumberException extends RuntimeException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
