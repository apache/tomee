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
package org.apache.openejb.client.proxy;

import org.apache.openejb.client.ClientRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class Jdk13ProxyFactory implements ProxyFactory {

    private final Class[] constructorParams = {java.lang.reflect.InvocationHandler.class};

    @Override
    public void init(final Properties props) {
        String version = "";
        final String badVersion = "1.3.0-";
        try {
            version = System.getProperty("java.vm.version");
        } catch (Exception e) {
            //Ignore
        }

        if (version.contains(badVersion)) {
            final String message = "" +
                "INCOMPATIBLE VM: \n\n" +
                "The Java Virtual Machine you are using contains a bug\n" +
                "in the proxy generation logic.  This bug has been    \n" +
                "documented by Sun and has been fixed in later VMs.   \n" +
                "Please download the latest 1.3 Virtual Machine.      \n" +
                "For more details see:                                \n" +
                "http://developer.java.sun.com/developer/bugParade/bugs/4346224.html\n  ";
            throw new ClientRuntimeException(message);
        }
    }

    @Override
    public InvocationHandler getInvocationHandler(final Object proxy) throws IllegalArgumentException {

        final Jdk13InvocationHandler handler = (Jdk13InvocationHandler) Proxy.getInvocationHandler(proxy);

        if (handler == null) {
            return null;
        }

        return handler.getInvocationHandler();
    }

    @Override
    public Object setInvocationHandler(final Object proxy, final InvocationHandler handler) throws IllegalArgumentException {

        final Jdk13InvocationHandler jdk13 = (Jdk13InvocationHandler) Proxy.getInvocationHandler(proxy);

        if (jdk13 == null) {
            throw new IllegalArgumentException("Proxy " + proxy + " unknown!");
        }

        return jdk13.setInvocationHandler(handler);
    }

    @Override
    public Class getProxyClass(final Class interfce) throws IllegalArgumentException {
        return Proxy.getProxyClass(interfce.getClassLoader(), new Class[]{interfce});
    }

    @Override
    public Class getProxyClass(final Class[] interfaces) throws IllegalArgumentException {
        if (interfaces.length < 1) {
            throw new IllegalArgumentException("There must be at least one interface to implement.");
        }

        return Proxy.getProxyClass(interfaces[0].getClassLoader(), interfaces);
    }

    @Override
    public boolean isProxyClass(final Class cl) {
        return Proxy.isProxyClass(cl);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object newProxyInstance(final Class proxyClass) throws IllegalArgumentException {
        if (!Proxy.isProxyClass(proxyClass)) {
            throw new IllegalArgumentException("This class is not a proxy.");
        }

        try {

            final Constructor cons = proxyClass.getConstructor(constructorParams);
            //noinspection RedundantArrayCreation
            return cons.newInstance(new Object[]{new Jdk13InvocationHandler()});

        } catch (final Exception e) {
            throw new InternalError(e.toString());
        }
    }

    @Override
    public Object newProxyInstance(final Class interfce, final InvocationHandler h) throws IllegalArgumentException {

        final Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);

        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, handler);
    }

    @Override
    public Object newProxyInstance(final Class[] interfaces, final InvocationHandler h) throws IllegalArgumentException {
        if (interfaces.length < 1) {
            throw new IllegalArgumentException("There must be at least one interface to implement.");
        }

        final Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);
        try {
            return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
        } catch (IllegalArgumentException iae) {
            final ClassLoader reconciliatedCl = reconciliate(interfaces);
            try {
                reconciliatedCl.loadClass(interfaces[0].getName());
                return Proxy.newProxyInstance(reconciliatedCl, interfaces, handler);
            } catch (ClassNotFoundException e2) {
                throw iae;
            }
        }
    }

    private static ClassLoader reconciliate(final Class<?>... interfaces) {
        final Set<ClassLoader> classloaders = new LinkedHashSet<ClassLoader>();
        for (final Class<?> clazz : interfaces) {
            classloaders.add(clazz.getClassLoader());
        }
        return new MultipleClassLoadersClassLoader(classloaders.toArray(new ClassLoader[classloaders.size()]));
    }

    private static class MultipleClassLoadersClassLoader extends ClassLoader {

        private ClassLoader[] delegatingClassloaders;

        public MultipleClassLoadersClassLoader(final ClassLoader[] classLoaders) {
            super(classLoaders[0]);
            delegatingClassloaders = classLoaders;
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            ClassNotFoundException ex = null;
            for (final ClassLoader cl : delegatingClassloaders) {
                try {
                    return cl.loadClass(name);
                } catch (ClassNotFoundException cnfe) {
                    if (ex == null) {
                        ex = cnfe;
                    }
                }
            }
            throw ex;
        }
    }
}
