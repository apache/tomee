package org.superbiz.injection.tx.api;

import javax.annotation.security.PermitAll;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metatype
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Read {
    public interface $ {

        @Read
        @PermitAll
        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        public void method();
    }
}
