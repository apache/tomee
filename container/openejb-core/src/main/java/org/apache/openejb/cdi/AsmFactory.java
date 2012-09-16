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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cdi;


import org.apache.webbeans.proxy.Factory;
import org.apache.webbeans.proxy.MethodHandler;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * @version $Rev$ $Date$
 */
public class AsmFactory implements Factory {

    public Object createProxy(MethodHandler handler, Class<?> superClass, Class<?>[] interfaceArray) {
        return AsmProxyFactory.newProxyInstance(WebBeansUtil.getCurrentClassLoader(), handler, superClass,
                interfaceArray);
    }

    public Class<?> getProxyClass(Class<?> superClass, Class<?>[] interfaces) {
        return AsmProxyFactory.getProxyClass(WebBeansUtil.getCurrentClassLoader(), superClass, interfaces);
    }

    public boolean isProxyInstance(Object o) {
        return AsmProxyFactory.isProxyClass(o.getClass());
    }

    public Object createProxy(MethodHandler handler, Class<?>[] interfaces)
            throws InstantiationException, IllegalAccessException {
        return createProxy(handler, null, interfaces);
    }

    public Object createProxy(Class<?> proxyClass)
            throws InstantiationException, IllegalAccessException {
        return AsmProxyFactory.constructProxy(proxyClass, null);
    }

    public void setHandler(Object proxy, MethodHandler handler) {
        AsmProxyFactory.setInvocationHandler(proxy, handler);
    }
}