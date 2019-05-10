package org.apache.tomee.microprofile.jwt;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * @version $Revision$ $Date$
 */
public class ConstraintAdapter<A extends Annotation, T> implements ConstraintValidator<A, T> {
    @Override
    public void initialize(final A constraintAnnotation) {
    }

    @Override
    public boolean isValid(final T value, final ConstraintValidatorContext context) {
        return isValid(value);
    }

    public boolean isValid(final T value) {
        return false;
    }
}
