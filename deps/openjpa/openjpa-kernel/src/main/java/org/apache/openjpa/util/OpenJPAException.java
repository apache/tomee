/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.openjpa.lib.util.Localizer.Message;

/**
 * Exception type for all OpenJPA exceptions. Meant to be easily
 * transformed into an appropriate exception at the API layer, since most APIs
 * define their own exception types.
 *
 * @author Abe White
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public abstract class OpenJPAException
    extends RuntimeException
    implements Serializable, ExceptionInfo {

    private transient boolean _fatal = false;
    private transient Object _failed = null;
    private transient Throwable[] _nested = null;

    /**
     * Default constructor.
     */
    public OpenJPAException() {
    }

    /**
     * Constructor; supply message.
     */
    public OpenJPAException(String msg) {
        super(msg);
    }

    /**
     * Constructor; supply message.
     */
    public OpenJPAException(Message msg) {
        super(msg.getMessage());
    }

    /**
     * Construct with cause.
     */
    public OpenJPAException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Construct with message and cause.
     */
    public OpenJPAException(String msg, Throwable cause) {
        super(msg);
        setCause(cause);
    }

    /**
     * Construct with message and cause.
     */
    public OpenJPAException(Message msg, Throwable cause) {
        super(msg.getMessage());
        setCause(cause);
    }

    /**
     * Exception type.
     */
    public abstract int getType();

    /**
     * Exception subtype.
     */
    public int getSubtype() {
        return 0;
    }

    /**
     * Whether this error is fatal.
     */
    public boolean isFatal() {
        return _fatal;
    }

    /**
     * Whether this error is fatal.
     */
    public OpenJPAException setFatal(boolean fatal) {
        _fatal = fatal;
        return this;
    }

    /**
     * Returns the first {@link Throwable} from {@link #getNestedThrowables}
     * in order to conform to {@link Throwable#getCause} in Java 1.4+.
     *
     * @see Throwable#getCause
     */
    public Throwable getCause() {
        if (_nested == null || _nested.length == 0)
            return null;
        else
            return _nested[0];
    }

    /**
     * The first nested throwable.
     */
    public OpenJPAException setCause(Throwable nested) {
        if (_nested != null)
            throw new IllegalStateException();
        if (nested != null)
            _nested = new Throwable[]{ nested };
        return this;
    }

    /**
     * The nested throwables.
     */
    public Throwable[] getNestedThrowables() {
        return (_nested == null) ? Exceptions.EMPTY_THROWABLES : _nested;
    }

    /**
     * The nested throwables.
     */
    public OpenJPAException setNestedThrowables(Throwable[] nested) {
        _nested = nested;
        return this;
    }

    /**
     * The failed object.
     */
    public Object getFailedObject() {
        return _failed;
    }

    /**
     * The failed object.
     */
    public OpenJPAException setFailedObject(Object failed) {
        _failed = failed;
        return this;
    }

    public String toString() {
        return Exceptions.toString(this);
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream out) {
        super.printStackTrace(out);
        Exceptions.printNestedThrowables(this, out);
    }

    public void printStackTrace(PrintWriter out) {
        super.printStackTrace(out);
        Exceptions.printNestedThrowables(this, out);
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        out.writeBoolean(_fatal);
        out.writeObject(Exceptions.replaceFailedObject(_failed));
        out.writeObject(Exceptions.replaceNestedThrowables(_nested));
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        _fatal = in.readBoolean();
        _failed = in.readObject();
        _nested = (Throwable[]) in.readObject ();
	}
}

