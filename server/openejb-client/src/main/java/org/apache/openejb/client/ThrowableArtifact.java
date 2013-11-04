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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
        final Stack<MockThrowable> stack = new Stack<MockThrowable>();

        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            stack.add(new MockThrowable(cause));
        }

        out.writeObject(stack);
        try {
            out.writeObject(throwable);
        } catch (IOException e) {
            //Ignore
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
