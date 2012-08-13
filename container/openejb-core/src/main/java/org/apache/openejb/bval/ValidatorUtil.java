/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.bval;

import org.apache.openejb.core.ThreadContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ValidatorUtil {
    private ValidatorUtil() {
         // no-op
    }

    public static ValidatorFactory validatorFactory() {
        try {
            return (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        } catch (NamingException e) {
            return proxy(ValidatorFactory.class, "java:comp/ValidatorFactory");
        }
    }

    public static Validator validator() {
        try {
            return (Validator) new InitialContext().lookup("java:comp/Validator");
        } catch (NamingException e) {
            return proxy(Validator.class, "java:comp/Validator");
        }
    }

    // proxy because depending on when injection/threadcontext is set
    // it is better to do it lazily
    // this is mainly done for tests since the first lookup will work in TomEE
    private static <T> T proxy(final Class<T> t, final String jndi) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{t},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (Object.class.equals(method.getDeclaringClass())) {
                            return method.invoke(this);
                        }

                        final ThreadContext ctx = ThreadContext.getThreadContext();
                        if (ctx != null) {
                            return method.invoke((T) ctx.getBeanContext().getJndiContext().lookup(jndi), args);
                        }
                        return null;
                    }

                    @Override
                    public String toString() {
                        return "Proxy::" + t.getName();
                    }
                });
    }
}
