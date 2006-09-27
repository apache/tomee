package org.apache.openejb.client;
/**
 * @version $Revision$ $Date$
 */

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ThrowableArtifactTest extends TestCase {

    public void testThrowableArtifact() throws Throwable {
        Throwable exception = new NullPointerException("ONE");
        exception = throwCatchReturn(exception);

        exception = new IllegalArgumentException("TWO", exception);
        exception = throwCatchReturn(exception);

        exception = new UnsupportedOperationException("THREE", exception);
        exception = throwCatchReturn(exception);

        exception = new IllegalStateException("FOUR", exception);
        exception = throwCatchReturn(exception);

        exception = new BadException("FIVE", exception);
        exception = throwCatchReturn(exception);

        String expectedStackTrace = getPrintedStackTrace(exception);

        ThrowableArtifact artifact = marshal(new ThrowableArtifact(exception));
        exception = throwCatchReturn(artifact.getThrowable().getCause());

        String actualStackTrace = getPrintedStackTrace(exception);

        assertEquals("stackTrace", expectedStackTrace, actualStackTrace);
    }

    private String getPrintedStackTrace(Throwable exception) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        exception.printStackTrace(printStream);
        printStream.flush();
        String stackTrace = new String(baos.toByteArray());
        return stackTrace;
    }

    private Throwable throwCatchReturn(Throwable exception) {
        try {
            throw exception;
        } catch (Throwable e) {
            return exception;
        }
    }

    private ThrowableArtifact marshal(ThrowableArtifact artifact) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(artifact);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        return (ThrowableArtifact) in.readObject();
    }

    public static class BadException extends Exception {
        private final Object data = new NotSerializableObject();

        public BadException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    public static class NotSerializableObject {
    }
}