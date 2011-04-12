package org.superbiz.corn.meta.api;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metatype
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@Singleton
@Lock(LockType.READ)
public @interface Organic {
}
