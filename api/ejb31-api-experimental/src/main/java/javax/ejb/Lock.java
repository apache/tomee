package javax.ejb;

import static javax.ejb.LockType.WRITE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;

@java.lang.annotation.Target(value = {METHOD, TYPE})
@java.lang.annotation.Retention(value = RUNTIME)
public @interface Lock {
    javax.ejb.LockType value() default WRITE;
}
