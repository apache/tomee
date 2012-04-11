package org.apache.openejb.itest.tomee;

public class ITRuntimeException extends RuntimeException {
    public ITRuntimeException(final Throwable e) {
        super(e);
    }
}
