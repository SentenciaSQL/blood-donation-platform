package com.afriasdev.dds.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class BloodTypeValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"})
    void valid_types_are_true(String bt) {
        assertThat(BloodTypeValidator.isValid(bt)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "O", "A*", "0+", "X-", "ab+", "null"})
    void invalid_types_are_false(String bt) {
        assertThat(BloodTypeValidator.isValid(bt)).isFalse();
    }

}
