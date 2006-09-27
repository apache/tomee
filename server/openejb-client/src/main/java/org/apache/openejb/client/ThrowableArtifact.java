/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class ThrowableArtifact implements Externalizable {

    private Throwable throwable;

    public ThrowableArtifact(Throwable throwable) {
        this.throwable = throwable;
    }

    public ThrowableArtifact() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        Stack<MockThrowable> stack = new Stack<MockThrowable>();

        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            stack.add(new MockThrowable(cause));
        }

        out.writeObject(stack);
        try {
            out.writeObject(throwable);
        } catch (IOException dontCare) {
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Stack<MockThrowable> stack = (Stack<MockThrowable>) in.readObject();
        try {
            throwable = (Throwable) in.readObject();
        } catch (Exception e) {
            throwable = createMockThrowable(stack); // recreate exception
        }
    }

    private Throwable createMockThrowable(Stack<MockThrowable> stack) {
        Throwable throwable = stack.pop();

        while (!stack.isEmpty()){
            throwable = stack.pop().initCause(throwable);
        }

        return new RuntimeException("The exception sent could not be serialized or deserialized.  This is a mock recreation:\n"+throwable, throwable);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    private static class MockThrowable extends Throwable {
        private final String classType;

        public MockThrowable(Throwable t){
            this(t.getMessage(),t.getClass().getName(), t.getStackTrace());
        }

        public MockThrowable(String message, String classType, StackTraceElement[] stackTrace) {
            super(message);
            this.classType = classType;
            this.setStackTrace(stackTrace);
        }

        public String toString() {
            String s = classType;
            String message = getLocalizedMessage();
            return (message != null) ? (s + ": " + message) : s;
        }
    }
}
