package org.apache.openejb.util.proxy;

import java.lang.reflect.Method;

public abstract class Proxy implements java.io.Serializable {

    public InvocationHandler handler;

    public InvocationHandler getInvocationHandler() {
        return handler;
    }

    public InvocationHandler setInvocationHandler(InvocationHandler newHandler) {
        InvocationHandler oldHandler = handler;
        handler = newHandler;
        return oldHandler;
    }

    protected static final Class[] NO_ARGS_C = new Class[0];

    protected static final Object[] NO_ARGS_O = new Object[0];

    protected final void _proxyMethod$throws_default$returns_void(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return;
    }

    protected final Object _proxyMethod$throws_default$returns_Object(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        java.lang.reflect.Method method = _proxyMethod$lookupMethod(methodNumber, methodName, argTypes);
        try {
            return handler.invoke(this, method, args);
        } catch (Throwable t) {

            if (t instanceof java.rmi.RemoteException)
                throw (java.rmi.RemoteException) t;
            if (t instanceof java.lang.RuntimeException)
                throw (java.lang.RuntimeException) t;
            else
                throw _proxyError$(t);
        }
    }

    protected final void _proxyMethod$throws_AppException$returns_void(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return;
    }

    protected final Object _proxyMethod$throws_AppException$returns_Object(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        java.lang.reflect.Method method = _proxyMethod$lookupMethod(methodNumber, methodName, argTypes);
        try {
            return handler.invoke(this, method, args);
        } catch (Throwable t) {

            if (t instanceof java.rmi.RemoteException)
                throw (java.rmi.RemoteException) t;
            if (t instanceof java.lang.RuntimeException)
                throw (java.lang.RuntimeException) t;
            if (t instanceof org.apache.openejb.ApplicationException)
                throw (org.apache.openejb.ApplicationException) t;
            else
                throw _proxyError$(t);

        }
    }

    protected final int _proxyMethod$throws_default$returns_int(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Integer retval = (Integer) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.intValue();
    }

    protected final double _proxyMethod$throws_default$returns_double(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Double retval = (Double) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.doubleValue();
    }

    protected final long _proxyMethod$throws_default$returns_long(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Long retval = (Long) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.longValue();
    }

    protected final boolean _proxyMethod$throws_default$returns_boolean(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Boolean retval = (Boolean) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.booleanValue();
    }

    protected final float _proxyMethod$throws_default$returns_float(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Float retval = (Float) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.floatValue();
    }

    protected final char _proxyMethod$throws_default$returns_char(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Character retval = (Character) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.charValue();
    }

    protected final byte _proxyMethod$throws_default$returns_byte(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Byte retval = (Byte) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.byteValue();
    }

    protected final short _proxyMethod$throws_default$returns_short(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException {
        Short retval = (Short) _proxyMethod$throws_default$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.shortValue();
    }

    protected final int _proxyMethod$throws_AppException$returns_int(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Integer retval = (Integer) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.intValue();
    }

    protected final double _proxyMethod$throws_AppException$returns_double(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Double retval = (Double) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.doubleValue();
    }

    protected final long _proxyMethod$throws_AppException$returns_long(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Long retval = (Long) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.longValue();
    }

    protected final boolean _proxyMethod$throws_AppException$returns_boolean(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Boolean retval = (Boolean) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.booleanValue();
    }

    protected final float _proxyMethod$throws_AppException$returns_float(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Float retval = (Float) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.floatValue();
    }

    protected final char _proxyMethod$throws_AppException$returns_char(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Character retval = (Character) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.charValue();
    }

    protected final byte _proxyMethod$throws_AppException$returns_byte(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Byte retval = (Byte) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.byteValue();
    }

    protected final short _proxyMethod$throws_AppException$returns_short(int methodNumber, String methodName, Class[] argTypes, Object[] args) throws java.rmi.RemoteException, org.apache.openejb.ApplicationException {
        Short retval = (Short) _proxyMethod$throws_AppException$returns_Object(methodNumber, methodName, argTypes, args);
        return retval.shortValue();
    }

    protected abstract Method _proxyMethod$lookupMethod(int index, String methodName, Class[] argTypes);

    protected final Method _proxyMethod$lookupMethod(Class interfce, Method [] methodMap, int index, String methodName, Class[] argTypes) {

        java.lang.reflect.Method method = methodMap[index];
        if (method == null) {
            try {
                method = interfce.getMethod(methodName, argTypes);
                methodMap[index] = method;
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException("Method not found:  " + nsme.getMessage());
            }
        }
        return method;
    }

    protected final java.rmi.RemoteException _proxyError$(Throwable throwable) {
        return new java.rmi.RemoteException("[OpenEJB]  Proxy Error: ", throwable);
    }

    protected final java.rmi.RemoteException _proxyError$(org.apache.openejb.ApplicationException ae) {
        return new java.rmi.RemoteException("[OpenEJB]  Proxy Error: The returned application exception is not defined in the throws clause.  ", ae.getRootCause());
    }

}
