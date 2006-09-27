package org.apache.openejb.alt.config;

public class ValidationError extends ValidationException {

    private Throwable cause;

    public ValidationError(String message) {
        super(message);
    }

    public String getPrefix() {
        return "ERROR";
    }

    public String getCategory() {
        return "errors";
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

}
