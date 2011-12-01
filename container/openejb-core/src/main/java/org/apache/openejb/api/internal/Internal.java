package org.apache.openejb.api.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used as a marker to specify than an object is internal.
 *
 * Used for instance for internal @Mbeans.
 *
 * @author rmannibucau
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
}
