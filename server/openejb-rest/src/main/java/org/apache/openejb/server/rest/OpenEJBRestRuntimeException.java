package org.apache.openejb.server.rest;

/**
 * @author Romain Manni-Bucau
 */
public class OpenEJBRestRuntimeException extends RuntimeException {
    public OpenEJBRestRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
