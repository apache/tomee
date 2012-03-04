package org.apache.openejb.tck;

public class OpenEJBTCKRuntimeException extends RuntimeException {
    public OpenEJBTCKRuntimeException(final Exception e) {
        super(e);
    }

    public OpenEJBTCKRuntimeException(final String str) {
        super(str);
    }
}
