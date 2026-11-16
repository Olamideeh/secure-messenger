package com.secure.messenger.service;

public interface SmsProvider {
    void sendSms(String phoneNumber, String message);
}
