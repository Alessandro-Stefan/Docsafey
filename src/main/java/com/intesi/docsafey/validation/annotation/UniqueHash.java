package com.intesi.docsafey.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intesi.docsafey.validation.validator.UniqueHashValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueHashValidator.class)
public @interface UniqueHash {
    
    String message() default "Hash must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
