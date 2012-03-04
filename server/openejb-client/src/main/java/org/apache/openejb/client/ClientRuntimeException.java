package org.apache.openejb.client;

public class ClientRuntimeException extends RuntimeException {
    public ClientRuntimeException(final String str) {
        super(str);
    }

    public ClientRuntimeException(final String str, final Throwable e) {
        super(str, e);
    }
}
