package com.letterbook.common;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    /** RF-13: mínimo 8 caracteres, ao menos uma letra e um dígito. */
    public static boolean isStrong(String pwd) {
        if (pwd == null || pwd.length() < 8) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }
}
