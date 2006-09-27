package org.apache.openejb.util.proxy;

import java.util.Properties;

import org.apache.openejb.OpenEJBException;

public interface ProxyFactory {

    public void init(Properties props) throws OpenEJBException;

    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException;

    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException;

    public Class getProxyClass(Class interfce) throws IllegalArgumentException;

    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException;

    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl);

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException;

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException;

    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException;
}

