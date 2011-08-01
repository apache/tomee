package org.apache.openejb.api;

import javax.persistence.PersistenceContext;
import java.lang.annotation.*;

/**
 * @author rmannibucau
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {
    PersistenceContext context() default @PersistenceContext;
    String jndiName() default "";
}

