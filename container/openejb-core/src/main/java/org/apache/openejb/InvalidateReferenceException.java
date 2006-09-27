package org.apache.openejb;

public class InvalidateReferenceException extends ApplicationException {

    public InvalidateReferenceException() {
        super();
    }

    public InvalidateReferenceException(String message) {
        super(message);
    }

    public InvalidateReferenceException(Exception e) {
        super(e);
    }

    public InvalidateReferenceException(Throwable t) {
        super(t);
    }

    public InvalidateReferenceException(String message, Exception e) {
        super(message, e);
    }

}
