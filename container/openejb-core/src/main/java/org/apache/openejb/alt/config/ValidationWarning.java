package org.apache.openejb.alt.config;

public class ValidationWarning extends ValidationException {

    public ValidationWarning(String message) {
        super(message);
    }

    public String getPrefix() {
        return "WARN";
    }

    public String getCategory() {
        return "warnings";
    }

}
