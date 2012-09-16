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
package org.apache.openejb.cdi.factory;

import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.webbeans.proxy.Factory;
import org.apache.webbeans.proxy.MethodHandler;
import org.apache.webbeans.util.WebBeansUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class ShadedAsmFactory implements Factory {
    private static final AtomicInteger ID = new AtomicInteger(1);

    private Object createProxy(final MethodHandler handler, final Class<?> superClass, final Class<?>[] interfaceArray) {
        return LocalBeanProxyFactory.newProxyInstance(WebBeansUtil.getCurrentClassLoader(), handler, superClass, interfaceArray);
    }

    @Override
    public Class<?> getProxyClass(final Class<?> classToProxy, final Class<?>[] interfaces) {
        final String proxyName;
        if (classToProxy == null || classToProxy.getName().startsWith("java.") ||
                classToProxy.getName().startsWith("javax.")) {
            proxyName = "BeanProxy$" + ID.incrementAndGet();
        } else {
            proxyName = classToProxy.getName() + "$BeanProxy";
        }

        return LocalBeanProxyFactory.createProxy(classToProxy, WebBeansUtil.getCurrentClassLoader(), proxyName, interfaces);
    }

    @Override
    public boolean isProxyInstance(final Object o) {
        return LocalBeanProxyFactory.isProxy(o.getClass());
    }

    @Override
    public Object createProxy(final MethodHandler handler, final Class<?>[] interfaces) throws InstantiationException, IllegalAccessException {
        return createProxy(handler, null, interfaces);
    }

    @Override
    public Object createProxy(final Class<?> proxyClass) throws InstantiationException, IllegalAccessException {
        return LocalBeanProxyFactory.constructProxy(proxyClass, NoHandler.INSTANCE);
    }

    @Override
    public void setHandler(final Object proxy, final MethodHandler handler) {
        LocalBeanProxyFactory.setInvocationHandler(proxy, handler);
    }

    private static class NoHandler implements InvocationHandler {
        private static final NoHandler INSTANCE = new NoHandler();

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new UnsupportedOperationException("No valid MethodHandler has been set");
        }
    }
}
