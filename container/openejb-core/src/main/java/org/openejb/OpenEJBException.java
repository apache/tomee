package org.openejb;

import java.io.PrintStream;
import java.io.PrintWriter;

public class OpenEJBException extends Exception {

    private String message = "error.unknown";

    private Throwable rootCause;

    public OpenEJBException() {
        super();
    }

    public OpenEJBException(String message) {
        super( message );
        this.message = message;
    }

    public OpenEJBException(Throwable rootCause) {
        this.rootCause = rootCause;
    }

    public OpenEJBException(String message, Throwable rootCause) {
        this( message );
        this.rootCause = rootCause;
    }

    public String getMessage() {
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (rootCause != null) {
            System.err.println("Root cause: ");
            rootCause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        if (rootCause != null) {
            stream.print("Root cause: ");
            rootCause.printStackTrace(stream);
        }
    }

    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (rootCause != null) {
            writer.print("Root cause: ");
            rootCause.printStackTrace(writer);
        }
    }

    public Throwable getRootCause() {
        return rootCause;
    }

}
