package com.afriasdev.dds.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidBloodTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBloodType {

    String message() default "Invalid blood type. Allowed values: O-, O+, A-, A+, B-, B+, AB-, AB+";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
