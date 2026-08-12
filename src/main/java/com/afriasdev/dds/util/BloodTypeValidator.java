package com.afriasdev.dds.util;

import com.afriasdev.dds.domain.BloodType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class BloodTypeValidator {

    private static final Set<String> ALLOWED = Arrays.stream(BloodType.values())
            .map(BloodType::getCode)
            .collect(Collectors.toUnmodifiableSet());

    private BloodTypeValidator() {
    }

    public static boolean isValid(String s) {
        return s != null && ALLOWED.contains(s);
    }

    public static BloodType parse(String s) {
        return BloodType.fromCode(s);
    }
}
