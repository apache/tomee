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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;

/**
 * @org.apache.xbean.XBean 
 */
public class Jdk13ProxyFactory implements ProxyFactory {

    public Jdk13ProxyFactory() {
    }

    public void init(Properties props) throws OpenEJBException {
    }

    public org.apache.openejb.util.proxy.InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        InvocationHandler handler = (InvocationHandler) Proxy.getInvocationHandler(proxy);
        if (handler == null) return null;
        return handler.getInvocationHandler();
    }

    public Class getProxyClass(Class interfce) throws IllegalArgumentException {
        return Proxy.getProxyClass(interfce.getClassLoader(), new Class[]{interfce});
    }

    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException {
        if (interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        return Proxy.getProxyClass(interfaces[0].getClassLoader(), interfaces);
    }

    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl) {
        return Proxy.isProxyClass(cl);
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, org.apache.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, h);
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, org.apache.openejb.util.proxy.InvocationHandler handler) throws IllegalArgumentException {
        if (interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }

        try {
            return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
        } catch (IllegalArgumentException e) {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Class tcclHomeClass = tccl.loadClass(interfaces[0].getName());
                if (tcclHomeClass == interfaces[0]) {
                    return Proxy.newProxyInstance(tccl, interfaces, handler);
                }
            } catch (ClassNotFoundException e1) {
                throw e;
            }
            throw e;
        }
    }
}

