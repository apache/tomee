/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.api.Proxy;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.MetaAnnotatedClass;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;

public class DynamicProxyImplFactory {
    public static boolean isKnownDynamicallyImplemented(Class<?> clazz) {
        final Annotated<Class<?>> metaClass = new MetaAnnotatedClass(clazz);
        return clazz.isInterface()
                && (metaClass.getAnnotation(PersistenceContext.class) != null
                || metaClass.getAnnotation(Proxy.class) != null);
    }

    public static Object newProxy(BeanContext context, java.lang.reflect.InvocationHandler invocationHandler) {
        if (invocationHandler instanceof QueryProxy) {
            EntityManager em = null;
            for (Injection injection : context.getInjections()) {
                if (QueryProxy.class.equals(injection.getTarget())) {
                    try {
                        em = (EntityManager) context.getJndiEnc().lookup(injection.getJndiName());
                    } catch (NamingException e) {
                        throw new OpenEJBRuntimeException("a dynamic bean should reference at least one correct PersistenceContext", e);
                    }
                }
            }
            if (em == null) {
                throw new OpenEJBRuntimeException("can't find the entity manager to use for the dynamic bean " + context.getEjbName());
            }
            ((QueryProxy) invocationHandler).setEntityManager(em);
        }

        try {
            return ProxyManager.newProxyInstance(context.getLocalInterface(), new Handler(invocationHandler));
        } catch (IllegalAccessException e) {
            throw new OpenEJBRuntimeException("illegal access", e);
        }
    }

    private static class Handler implements InvocationHandler {
        private java.lang.reflect.InvocationHandler handler;

        private Handler(java.lang.reflect.InvocationHandler handler) {
            this.handler = handler;
        }

        @Override public InvocationHandler getInvocationHandler() {
            return this;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return handler.invoke(proxy, method, args);
        }
    }
}
