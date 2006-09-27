package org.apache.openejb.core.ivm.naming;

public class NamingException extends javax.naming.NamingException {
    private org.apache.openejb.OpenEJBException delegate;

    public NamingException(String message, org.apache.openejb.OpenEJBException delegateArg) {
        super();
        delegate = delegateArg;
    }

    public NamingException(String message, Throwable rootCause) {
        super();
        delegate = new org.apache.openejb.OpenEJBException(message, rootCause);
    }

    public String getMessage() {
        return delegate.getMessage();
    }

    public void printStackTrace() {
        delegate.printStackTrace();
    }

    public void printStackTrace(java.io.PrintStream stream) {
        delegate.printStackTrace(stream);
    }

    public void printStackTrace(java.io.PrintWriter writer) {
        delegate.printStackTrace(writer);
    }
}
