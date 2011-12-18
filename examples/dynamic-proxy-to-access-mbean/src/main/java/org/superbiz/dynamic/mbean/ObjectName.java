package org.superbiz.dynamic.mbean;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface ObjectName {
    String value();

    // for remote usage only
    String url() default "";
    String user() default "";
    String password() default "";
}
