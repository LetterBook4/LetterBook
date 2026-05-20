package com.letterbook.common;

import java.util.regex.Pattern;

public final class EmailValidator {
    private static final Pattern EMAIL =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailValidator() {}

    public static boolean isValid(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > 254) return false;
        return EMAIL.matcher(trimmed).matches();
    }
}
