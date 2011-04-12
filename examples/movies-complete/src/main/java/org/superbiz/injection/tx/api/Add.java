package org.superbiz.injection.tx.api;

import org.superbiz.injection.tx.AddInterceptor;

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

public @interface Add {
    public interface $ {

        @Add
        @RolesAllowed({"Employee", "Manager"})
        @TransactionAttribute(TransactionAttributeType.REQUIRED)
        @Interceptors(AddInterceptor.class)
        public void method();
    }
}