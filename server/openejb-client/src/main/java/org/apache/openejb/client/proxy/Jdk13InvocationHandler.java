package org.apache.openejb.client.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

public class Jdk13InvocationHandler implements java.lang.reflect.InvocationHandler, Serializable {

    private InvocationHandler delegate;

    public Jdk13InvocationHandler() {
    }

    public Jdk13InvocationHandler(InvocationHandler delegate) {
        setInvocationHandler(delegate);
    }

    public InvocationHandler getInvocationHandler() {
        return delegate;
    }

    public InvocationHandler setInvocationHandler(InvocationHandler handler) {
        InvocationHandler old = delegate;
        delegate = handler;
        return old;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (delegate == null) throw new NullPointerException("No invocation handler for proxy " + proxy);

        if (args == null) {
            args = new Object[0];

        }

        return delegate.invoke(proxy, method, args);
    }
}

