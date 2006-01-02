package org.openejb.alt.config;

public class ValidationError extends ValidationException {

    public ValidationError(String message) {
        super(message);
    }

    public String getPrefix() {
        return "ERROR";
    }

    public String getCategory() {
        return "errors";
    }

}
