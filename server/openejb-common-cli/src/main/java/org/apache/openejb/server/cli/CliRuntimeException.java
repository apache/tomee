package org.apache.openejb.server.cli;

import java.io.IOException;

public class CliRuntimeException extends RuntimeException {
    public CliRuntimeException(Exception e) {
        super(e);
    }
}
