package org.apache.openejb.arquillian.common;

public class ArquillianRuntimeException extends RuntimeException {
    public ArquillianRuntimeException(final String str) {
        super(str);
    }

    public ArquillianRuntimeException(final String str, final Throwable e) {
        super(str, e);
    }

    public ArquillianRuntimeException(final Throwable e) {
        super(e);
    }
}
