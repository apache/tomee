package org.apache.openejb.loader;

public class LoaderRuntimeException extends RuntimeException {
    public LoaderRuntimeException(final String str) {
        super(str);
    }

    public LoaderRuntimeException(final String str, final Throwable e) {
        super(str, e);
    }

    public LoaderRuntimeException(final Exception e) {
        super(e);
    }
}
