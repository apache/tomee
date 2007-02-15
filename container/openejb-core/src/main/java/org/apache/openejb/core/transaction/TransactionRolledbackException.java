/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.transaction;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Subclass of javax.transaction.TransactionRolledbackException which adds init cause to the exception.
 */
public class TransactionRolledbackException extends javax.transaction.TransactionRolledbackException {
    private Throwable cause = this;

    public TransactionRolledbackException() {
        super();
        fillInStackTrace();
    }

    public TransactionRolledbackException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance of this class with its walkback, message and
     * cause filled in.
     *
     * @param detailMessage String The detail message for the exception.
     * @param throwable The cause of this Throwable
     */
    public TransactionRolledbackException(String detailMessage, Throwable throwable) {
        super(detailMessage);
        cause = throwable;
    }

    public TransactionRolledbackException(Throwable throwable) {
        super(throwable == null ? null : throwable.toString());
        cause = throwable;
    }

    /**
     * Answers the extra information message which was provided when the
     * throwable was created. If no message was provided at creation time, then
     * answer null. Subclasses may override this method to answer localized text
     * for the message.
     *
     * @return String The receiver's message.
     */
    public String getLocalizedMessage() {
        return getMessage();
    }

    /**
     * Outputs a printable representation of the receiver's walkback on the
     * System.err stream.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Count the number of duplicate stack frames, starting from
     * the end of the stack.
     *
     * @param currentStack a stack to compare
     * @param parentStack a stack to compare
     * @return the number of duplicate stack frames.
     */
    private static int countDuplicates(StackTraceElement[] currentStack,
            StackTraceElement[] parentStack) {
        int duplicates = 0;
        int parentIndex = parentStack.length;
        for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
            StackTraceElement parentFrame = parentStack[parentIndex];
            if (parentFrame.equals(currentStack[i])) {
                duplicates++;
            } else {
                break;
            }
        }
        return duplicates;
    }

    /**
     * Outputs a printable representation of the receiver's walkback on the
     * stream specified by the argument.
     *
     * @param err PrintStream The stream to write the walkback on.
     */
    public void printStackTrace(PrintStream err) {
        err.println(toString());
        // Don't use getStackTrace() as it calls clone()
        // Get stackTrace, in case stackTrace is reassigned
        StackTraceElement[] stack = getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            err.println("\tat " + stack[i]);
        }

        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack = throwable.getStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }

    /**
     * Outputs a printable representation of the receiver's walkback on the
     * writer specified by the argument.
     *
     * @param err PrintWriter The writer to write the walkback on.
     */
    public void printStackTrace(PrintWriter err) {
        err.println(toString());
        // Don't use getStackTrace() as it calls clone()
        // Get stackTrace, in case stackTrace is reassigned
        StackTraceElement[] stack = getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            err.println("\tat " + stack[i]);
        }

        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack = throwable.getStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }

    /**
     * Answers a string containing a concise, human-readable description of the
     * receiver.
     *
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        String msg = getLocalizedMessage();
        String name = getClass().getName();
        if (msg == null) {
            return name;
        }
        return new StringBuffer(name.length() + 2 + msg.length()).append(name).append(": ").append(msg).toString();
    }

    /**
     * Initialize the cause of the receiver. The cause cannot be reassigned.
     *
     * @param throwable The cause of this Throwable
     * @return the receiver.
     * @throws IllegalArgumentException when the cause is the receiver
     * @throws IllegalStateException when the cause has already been initialized
     */
    public synchronized TransactionRolledbackException initCause(Throwable throwable) {
        cause = throwable;
        return this;
    }

    /**
     * Answers the cause of this Throwable, or null if there is no cause.
     *
     * @return Throwable The receiver's cause.
     */
    public Throwable getCause() {
        if (cause == this) {
            return null;
        }
        return cause;
    }
}
