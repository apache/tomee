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

import org.apache.openejb.util.Debug;

import java.lang.reflect.Field;

public class LocalBeanProxyFactory {

    public static Object newProxyInstance(ClassLoader cl, Class interfce, java.lang.reflect.InvocationHandler h) throws IllegalArgumentException {
        try {
            final LocalBeanProxyGeneratorImpl generator = new LocalBeanProxyGeneratorImpl();

            final Class proxyClass = generator.createProxy(interfce, cl);
            final Object object = generator.constructProxy(proxyClass, h);

            return object;
        } catch (Throwable e) {
            throw new InternalError(Debug.printStackTrace(e));
        }
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        try {
            final Field field = proxy.getClass().getDeclaredField(LocalBeanProxyGeneratorImpl.BUSSINESS_HANDLER_NAME);
            field.setAccessible(true);
            try {
                return (InvocationHandler) field.get(proxy);
            } finally {
                field.setAccessible(false);
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
