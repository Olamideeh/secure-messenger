package com.secure.messenger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class MockSmsProvider implements SmsProvider {

 private static final Logger logger
         = LoggerFactory.getLogger(MockSmsProvider.class);
    @Override
    public void sendSms(String phoneNumber, String message) {
    logger.info("[MOCK SMS ] Recipent : {} Message: {}", phoneNumber, message);
    }
}
