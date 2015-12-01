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

package org.apache.openejb.web;

import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class WebInitialContext implements InvocationHandler {
    private static final Class<?>[] INTERFACES = new Class<?>[]{Context.class};

    private final Map<String, Object> bindings;
    private final Context delegate;

    public WebInitialContext(final Map<String, Object> bindings, final Context ctx) {
        this.bindings = bindings;
        delegate = ctx;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if ("lookup".equals(method.getName()) && method.getParameterTypes().length == 1 && String.class.equals(method.getParameterTypes()[0])) {
            final Object lookedUp = bindings.get(normalize((String) args[0]));
            if (lookedUp != null) {
                if (lookedUp instanceof Reference) {
                    return ((Reference) lookedUp).getObject();
                } else if (lookedUp instanceof LinkRef) {
                    return ((Context) proxy).lookup(((LinkRef) lookedUp).getLinkName());
                }
                try {
                    return method.invoke(delegate, args);
                } catch (final InvocationTargetException nnfe) {
                    if (NameNotFoundException.class.isInstance(nnfe.getTargetException())) {
                        return lookedUp;
                    }
                    throw nnfe.getTargetException();
                }
            }
        }
        try {
            return method.invoke(delegate, args);
        } catch (final InvocationTargetException nnfe) {
            throw nnfe.getTargetException();
        }
    }

    private static String normalize(final String arg) {
        if (arg.startsWith("java:")) {
            return arg.substring("java:".length());
        }
        return arg;
    }

    public static Context create(final Map<String, Object> bindings, final Context fallback) {
        return (Context) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), INTERFACES, new WebInitialContext(bindings, fallback));
    }
}
