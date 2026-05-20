package com.letterbook.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "user@test.com",
        "a.b+tag@example.co",
        "joao.silva@empresa.com.br",
        "x@y.io"
    })
    void valid(String email) { assertTrue(EmailValidator.isValid(email)); }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "no-at.com",
        "@no-local.com",
        "no-domain@",
        "spaces in@mail.com",
        "double@@at.com",
        "trailing.dot@x.c"
    })
    void invalid(String email) { assertFalse(EmailValidator.isValid(email)); }
}
