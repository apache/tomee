package org.apache.openejb.server;

public class ServerRuntimeException extends RuntimeException {
    public ServerRuntimeException(final String str) {
        super(str);
    }

    public ServerRuntimeException(final String str, final Throwable e) {
        super(str, e);
    }

    public ServerRuntimeException(final Exception e) {
        super(e);
    }
}
