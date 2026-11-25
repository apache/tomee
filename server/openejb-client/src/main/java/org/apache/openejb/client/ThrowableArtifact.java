/**
 *
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
package org.apache.openejb.client;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ThrowableArtifact implements Externalizable {

    private static final long serialVersionUID = 8465895155478377443L;
    private transient Throwable throwable;
    private transient ProtocolMetaData metaData;

    public ThrowableArtifact(final Throwable throwable) {
        this.throwable = throwable;
    }

    public ThrowableArtifact() {
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final Stack<MockThrowable> stack = new Stack<>();

        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            stack.add(new MockThrowable(cause));
        }

        out.writeObject(stack);
        try {
            out.writeObject(isSerializable(throwable) ? throwable : portable(throwable));
        } catch (final IOException | RuntimeException | LinkageError e) {
            //Ignore, readExternal recreates the exception from the stack
        }
    }

    /**
     * Recreates a chain that cannot be serialized, e.g. because one of its exceptions belongs to an application
     * whose class loader is already stopped (TOMEE-4736). Every exception keeps its type when a copy of it can be
     * serialized, the others are replaced by a stand-in with their class name, message and stack trace.
     */
    private static Throwable portable(final Throwable throwable) {
        final List<Throwable> chain = new ArrayList<>();
        for (Throwable cause = throwable; cause != null && !chain.contains(cause); cause = cause.getCause()) {
            chain.add(cause);
        }

        Throwable portable = null;
        for (int i = chain.size() - 1; i >= 0; i--) {
            final Throwable original = chain.get(i);
            Throwable copy = copy(original, portable);
            if (copy == null || !isSerializable(copy)) {
                copy = new MockThrowable(original);
                copy.initCause(portable);
            }
            copy.setStackTrace(original.getStackTrace());
            portable = copy;
        }

        return new ClientRuntimeException("The exception sent could not be serialized.  This is a recreation:\n" + throwable, portable);
    }

    private static Throwable copy(final Throwable original, final Throwable cause) {
        final Class<? extends Throwable> type = original.getClass();
        try {
            return type.getConstructor(String.class, Throwable.class).newInstance(original.getMessage(), cause);
        } catch (final Exception | LinkageError e) {
            // no (String, Throwable) constructor
        }
        try {
            final Throwable copy = type.getConstructor(String.class).newInstance(original.getMessage());
            if (cause != null) {
                copy.initCause(cause);
            }
            return copy;
        } catch (final Exception | LinkageError e) {
            return null;
        }
    }

    private static boolean isSerializable(final Throwable throwable) {
        try (final ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
            out.writeObject(throwable);
            return true;
        } catch (final Exception | LinkageError e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final Stack<MockThrowable> stack = (Stack<MockThrowable>) in.readObject();
        try {
            throwable = (Throwable) in.readObject();
        } catch (Exception e) {
            throwable = createMockThrowable(stack); // recreate exception
        }
    }

    private Throwable createMockThrowable(final Stack<MockThrowable> stack) {
        Throwable throwable = stack.pop();

        while (!stack.isEmpty()) {
            throwable = stack.pop().initCause(throwable);
        }

        return new ClientRuntimeException("The exception sent could not be serialized or deserialized.  This is a mock recreation:\n" + throwable, throwable);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String toString() {
        return throwable.toString();
    }

    private static class MockThrowable extends Throwable {

        private final String classType;

        public MockThrowable(final Throwable t) {
            this(t.getMessage(), t.getClass().getName(), t.getStackTrace());
        }

        public MockThrowable(final String message, final String classType, final StackTraceElement[] stackTrace) {
            super(message);
            this.classType = classType;
            this.setStackTrace(stackTrace);
        }

        public String toString() {
            final String s = classType;
            final String message = getLocalizedMessage();
            return (message != null) ? (s + ": " + message) : s;
        }
    }
}
