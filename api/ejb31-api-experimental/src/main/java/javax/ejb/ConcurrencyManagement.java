package javax.ejb;

import static javax.ejb.ConcurrencyManagementType.CONTAINER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;

@java.lang.annotation.Target(value = {METHOD, TYPE})
@java.lang.annotation.Retention(value = RUNTIME)
public @interface ConcurrencyManagement {
    javax.ejb.ConcurrencyManagementType value() default CONTAINER;
}
