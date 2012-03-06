package org.superbiz.dynamic.api;

import org.apache.openejb.api.Proxy;
import org.superbiz.dynamic.framework.SpringDataProxy;

import javax.ejb.Stateless;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metatype
@Stateless
@Proxy(SpringDataProxy.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringRepository {
}

