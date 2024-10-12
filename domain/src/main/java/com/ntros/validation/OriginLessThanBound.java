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
public @interface OriginLessThanBound {

    String message() default "Origin must be greater than Bound";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
