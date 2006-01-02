package org.openejb.util.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

public class Jdk13InvocationHandler implements java.lang.reflect.InvocationHandler, Serializable {
    private org.openejb.util.proxy.InvocationHandler delegate;

    public Jdk13InvocationHandler() {
    }

    public Jdk13InvocationHandler(org.openejb.util.proxy.InvocationHandler delegate) {
        setInvocationHandler(delegate);
    }

    public org.openejb.util.proxy.InvocationHandler getInvocationHandler() {
        return delegate;
    }

    public org.openejb.util.proxy.InvocationHandler setInvocationHandler(org.openejb.util.proxy.InvocationHandler handler) {
        org.openejb.util.proxy.InvocationHandler old = delegate;
        delegate = handler;
        return old;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(delegate != null) {
            if(args == null) {
                args = new Object[0];

            }
            return delegate.invoke(proxy, method, args);
        } else {
            throw new NullPointerException("No invocation handler for proxy "+proxy);
        }
    }
}

