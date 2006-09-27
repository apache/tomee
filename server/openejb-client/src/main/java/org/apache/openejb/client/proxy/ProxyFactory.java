package org.apache.openejb.client.proxy;

import java.util.Properties;

public interface ProxyFactory {

    public void init(Properties props);

    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException;

    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException;

    public Class getProxyClass(Class interfce) throws IllegalArgumentException;

    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException;

    public boolean isProxyClass(Class cl);

    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException;

    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException;

    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException;
}

