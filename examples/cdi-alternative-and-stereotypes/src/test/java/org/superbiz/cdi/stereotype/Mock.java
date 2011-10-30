package org.superbiz.cdi.stereotype;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author rmannibucau
 */
// defining a stereotype for class level
@Stereotype
@Retention(RUNTIME)
@Target(TYPE)

// here define all annotations you want to replace by this one.
// this stereotype define an alternative
@Alternative
public @interface Mock {
}
