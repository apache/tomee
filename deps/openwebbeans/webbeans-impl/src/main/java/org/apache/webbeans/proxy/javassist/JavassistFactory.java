/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.proxy.javassist;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.apache.webbeans.proxy.Factory;
import org.apache.webbeans.proxy.MethodHandler;

/**
* @version $Rev$ $Date$
*/
public class JavassistFactory
    implements Factory
{
    public Class<?> getProxyClass(Class<?> superClass, Class<?>[] interfaces)
    {
        ProxyFactory fact = new ProxyFactory();
        fact.setInterfaces(interfaces);
        fact.setSuperclass(superClass);
        fact.setFilter(FinalizeMethodFilter.INSTANCE);

        return getProxyClass(fact);
    }

    private static Class<?> getProxyClass(ProxyFactory factory)
    {
        ProxyFactory.ClassLoaderProvider classLoaderProvider = ProxyFactory.classLoaderProvider;
        try
        {
           return doPrivilegedCreateClass(factory);
        }
        catch(RuntimeException e)
        {
            if(classLoaderProvider instanceof OpenWebBeansClassLoaderProvider)
            {
                ((OpenWebBeansClassLoaderProvider)classLoaderProvider).useCurrentClassLoader();
            }

            //try again with updated class loader
            return doPrivilegedCreateClass(factory);
        }
        finally
        {
            if(classLoaderProvider instanceof OpenWebBeansClassLoaderProvider)
            {
                ((OpenWebBeansClassLoaderProvider)classLoaderProvider).reset();
            }
        }
    }

    public Object createProxy(Class<?> proxyClass)
        throws InstantiationException, IllegalAccessException
    {
        return proxyClass.newInstance();
    }


    private static Class<?> doPrivilegedCreateClass(ProxyFactory factory)
    {
        if (System.getSecurityManager() == null)
        {
            return factory.createClass();
        }
        else
        {
            return (Class<?>) AccessController.doPrivileged(new PrivilegedActionForProxyFactory(factory));
        }
    }

    /**
     * @param o the object to check
     * @return <code>true</code> if the given object is a proxy
     */
    public boolean isProxyInstance(Object o)
    {
        return o instanceof ProxyObject;
    }

    public Object createProxy(MethodHandler handler, Class<?>[] interfaces)
        throws InstantiationException, IllegalAccessException
    {
        ProxyFactory pf = new ProxyFactory();
        pf.setInterfaces(interfaces);
        pf.setHandler(handler);

        return getProxyClass(pf).newInstance();
    }

    public void setHandler(Object proxy, MethodHandler handler)
    {
        ((ProxyObject)proxy).setHandler(handler);
    }


    protected static class PrivilegedActionForProxyFactory implements PrivilegedAction<Object>
    {
        private ProxyFactory factory;

        protected PrivilegedActionForProxyFactory(ProxyFactory factory)
        {
            this.factory = factory;
        }

        public Object run()
        {
            return factory.createClass();
        }
    }

    public static class FinalizeMethodFilter implements MethodFilter
    {
        private static final String FINALIZE = "finalize".intern();

        public static final FinalizeMethodFilter INSTANCE = new FinalizeMethodFilter();

        public boolean isHandled(final Method method)
        {
            return !(method.getName() == FINALIZE
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() == Void.TYPE);
        }
    }
}
