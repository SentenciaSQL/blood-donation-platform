package com.afriasdev.dds.validation;

import com.afriasdev.dds.util.BloodTypeValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBloodTypeValidator implements ConstraintValidator<ValidBloodType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // let @NotBlank handle emptiness
        }
        return BloodTypeValidator.isValid(value);
    }
}
