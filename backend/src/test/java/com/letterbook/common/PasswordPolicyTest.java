package com.letterbook.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"abcdef12", "Senha123", "MyP@ss99", "longpassword1"})
    void strong(String s) { assertTrue(PasswordPolicy.isStrong(s)); }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"short1a", "12345678", "onlyletters", "abc12", "abcdefgh"})
    void weak(String s) { assertFalse(PasswordPolicy.isStrong(s)); }
}
