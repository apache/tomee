package org.apache.openejb;

public class OpenEJBRuntimeException extends RuntimeException {
    public OpenEJBRuntimeException(final String str) {
        super(str);
    }

    public OpenEJBRuntimeException(final String str, final Throwable e) {
        super(str, e);
    }

    public OpenEJBRuntimeException(final Exception e) {
        super(e);
    }
}
