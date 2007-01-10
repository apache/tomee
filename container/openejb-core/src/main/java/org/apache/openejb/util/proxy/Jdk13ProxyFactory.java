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
        start();
    }

    /**
     * @org.apache.xbean.InitMethod
     */
    public void start() throws OpenEJBException {
        String version = "";
        String badVersion = "1.3.0-";
        try {
            version = System.getProperty("java.vm.version");
        } catch (Exception e) {
        }
        if (version.indexOf(badVersion) != -1) {
            String message = "" +
                    "INCOMPATIBLE VM: \n\n" +
                    "The Java Virtual Machine you are using contains a bug\n" +
                    "in the proxy generation logic.  This bug has been    \n" +
                    "documented by Sun and has been fixed in later VMs.   \n" +
                    "Please download the latest 1.3 Virtual Machine.      \n" +
                    "For more details see:                                    \n" +
                    "http://developer.java.sun.com/developer/bugParade/bugs/4346224.html\n  ";
            throw new OpenEJBException(message);
        }
    }

    public org.apache.openejb.util.proxy.InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        Jdk13InvocationHandler handler = (Jdk13InvocationHandler) Proxy.getInvocationHandler(proxy);
        if (handler == null)
            return null;
        return handler.getInvocationHandler();
    }

    public Object setInvocationHandler(Object proxy, org.apache.openejb.util.proxy.InvocationHandler handler) throws IllegalArgumentException {
        Jdk13InvocationHandler jdk13 = (Jdk13InvocationHandler) Proxy.getInvocationHandler(proxy);
        if (jdk13 == null)
            throw new IllegalArgumentException("Proxy " + proxy + " unknown!");
        return jdk13.setInvocationHandler(handler);
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

    private final static Class[] constructorParams = {java.lang.reflect.InvocationHandler.class};

    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException {
        if (!Proxy.isProxyClass(proxyClass))
            throw new IllegalArgumentException();
        try {
            Constructor cons = proxyClass.getConstructor(constructorParams);
            return (Object) cons.newInstance(new Object[]{new Jdk13InvocationHandler()});
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, org.apache.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);
        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, handler);
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, org.apache.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        if (interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        Jdk13InvocationHandler handler = new Jdk13InvocationHandler(h);
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
    }
}

