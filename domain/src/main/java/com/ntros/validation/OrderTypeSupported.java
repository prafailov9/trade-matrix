package com.ntros.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = OriginLessThanBoundValidator.class)
@Documented
public @interface OrderTypeSupported {
    String message() default "Given order type not supported";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

