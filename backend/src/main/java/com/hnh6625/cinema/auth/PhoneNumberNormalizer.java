package com.hnh6625.cinema.auth;

import com.hnh6625.cinema.auth.exception.InvalidPhoneNumberException;

import java.util.regex.Pattern;

public final class PhoneNumberNormalizer {
    private static final Pattern NATIONAL_MOBILE = Pattern.compile("0[35789]\\d{8}");

    private PhoneNumberNormalizer() {};

    public static String normalize(String rawPhone) {

        if (rawPhone == null || rawPhone.isBlank()) {
            throw new InvalidPhoneNumberException("Phone number is required");
        }

        String cleaned = rawPhone.replaceAll("[\\s.-]", "");

        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("84")) {
            cleaned = "0" + cleaned.substring(2);
        }

        if (!NATIONAL_MOBILE.matcher(cleaned).matches()) {
            throw new InvalidPhoneNumberException("Invalid Vietnamese mobile phone number");
        }

        return "+84" + cleaned.substring(1);
    }

}
