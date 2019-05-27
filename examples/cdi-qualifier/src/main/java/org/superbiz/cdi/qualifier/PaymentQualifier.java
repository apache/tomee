package org.superbiz.cdi.qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * 
 * Qualifier that help CDI the choose the right implementation in the inject
 * <br>
 * This qualifier have a {@link PaymentType}, this <b>enum</b> contain all options to separate implementations.
 * 
 * */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Qualifier
public @interface PaymentQualifier {

	PaymentType type();	
}
