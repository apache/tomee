package org.apache.openejb.arquillian.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @version $Rev$ $Date$
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Prefixes {
    String[] value();
}
