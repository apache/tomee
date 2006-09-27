package org.apache.openejb;

public class OpenEJBException extends Exception {

    private String message = "error.unknown";

    public OpenEJBException() {
        super();
    }

    public OpenEJBException(String message) {
        super(message);
        this.message = message;
    }

    public OpenEJBException(Throwable rootCause) {
        super(rootCause);
    }

    public OpenEJBException(String message, Throwable rootCause) {
        super(message, rootCause);
        this.message = message;
    }

    public String getMessage() {
        Throwable rootCause = getCause();
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    public Throwable getRootCause() {
        return super.getCause();
    }
}
