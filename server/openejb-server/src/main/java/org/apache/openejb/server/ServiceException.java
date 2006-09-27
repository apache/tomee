package org.apache.openejb.server;

import org.apache.openejb.OpenEJBException;

public class ServiceException extends OpenEJBException {

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable rootCause) {
        super(rootCause);
    }

    public ServiceException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

}

