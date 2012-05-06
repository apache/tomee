package org.apache.openejb.itest.tomee;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD })
@Retention(RUNTIME)
public @interface Library {
    String value() default ITTomEERunner.DEFAULT_SERVER; // server name
}
