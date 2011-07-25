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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBException;

public class LocalBeanProxyFactory {

    private static final java.lang.reflect.InvocationHandler NON_BUSINESS_HANDLER = new NonBusinessHandler();
    
    public static Object newProxyInstance(ClassLoader cl, Class interfce, java.lang.reflect.InvocationHandler h) throws IllegalArgumentException {
            try {
            Class proxyCls = new LocalBeanProxyGeneratorImpl().createProxy(interfce, cl);
            Constructor constructor = proxyCls.getConstructor(java.lang.reflect.InvocationHandler.class, 
                                                              java.lang.reflect.InvocationHandler.class);
            Object object = constructor.newInstance(h, NON_BUSINESS_HANDLER);
            return object;
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        try {
            Field field = proxy.getClass().getDeclaredField(LocalBeanProxyGeneratorImpl.BUSSINESS_HANDLER_NAME);
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
    
    private static class NonBusinessHandler implements java.lang.reflect.InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new EJBException("Calling non-public methods of a local bean without any interfaces is not allowed");
        }
        
    }
}
