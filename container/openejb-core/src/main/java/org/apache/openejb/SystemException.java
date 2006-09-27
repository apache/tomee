package org.apache.openejb;

public class SystemException extends OpenEJBException {

    public SystemException() {
        super();
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable rootCause) {
        super(rootCause);
    }

    public SystemException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

}
