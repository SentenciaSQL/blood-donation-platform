package com.afriasdev.dds.util;

import java.util.Set;

public class BloodTypeValidator {
    private static final Set<String> ALLOWED = Set.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+");

    public static boolean isValid(String s) {
        return s != null && ALLOWED.contains(s);
    }
}
