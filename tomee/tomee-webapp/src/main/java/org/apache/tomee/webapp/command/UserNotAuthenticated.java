package org.apache.tomee.webapp.command;

import org.apache.tomee.webapp.TomeeException;

public class UserNotAuthenticated extends TomeeException {
    private static final String MESSAGE = "User not authenticated";

    public UserNotAuthenticated(Throwable cause) {
        super(MESSAGE, cause);
    }

    public UserNotAuthenticated() {
        super(MESSAGE);
    }
}
