package org.apache.openejb;

public class ApplicationException extends OpenEJBException {

    public ApplicationException() {
        super();
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Exception e) {
        super(e);
    }

    public ApplicationException(Throwable t) {
        super(t);
    }

    public ApplicationException(String message, Exception e) {
        super(message, e);
    }
}