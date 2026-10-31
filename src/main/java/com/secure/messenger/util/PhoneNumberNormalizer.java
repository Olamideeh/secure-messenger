package com.secure.messenger.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.secure.messenger.exception.InvalidPhoneNumberException;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {

    private final PhoneNumberUtil phoneNumberUtil =
            PhoneNumberUtil.getInstance();

    public String normalize(String phoneNumber, String region) {

        try {
            Phonenumber.PhoneNumber parsedNumber =
                    phoneNumberUtil.parse(phoneNumber, region);

            if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                throw new InvalidPhoneNumberException("Invalid phone number");
            }

            return phoneNumberUtil.format(
                    parsedNumber,
                    PhoneNumberUtil.PhoneNumberFormat.E164
            );

        } catch (NumberParseException exception) {
            throw new InvalidPhoneNumberException ("Invalid phone number");
        }
    }
}