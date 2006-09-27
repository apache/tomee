package org.apache.openejb.util.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;

public class InvalidatedReferenceHandler implements InvocationHandler, Serializable {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new NoSuchObjectException("reference is invalid");
    }
}
