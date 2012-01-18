package org.apache.openejb.server.ssh;

public class SshRuntimeException extends RuntimeException {
    public SshRuntimeException(Exception e) {
        super(e);
    }
}
