package org.superbiz.injection.tx.api;

import org.superbiz.injection.tx.DeleteInterceptor;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metatype
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Delete {
    public interface $ {

        @Delete
        @RolesAllowed({"Manager"})
        @TransactionAttribute(TransactionAttributeType.MANDATORY)
        @Interceptors(DeleteInterceptor.class)
        public void method();
    }
}