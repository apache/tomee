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
package org.apache.openjpa.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.openjpa.util.ExceptionInfo;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.UserException;
import org.apache.openjpa.lib.util.Localizer.Message;

/**
 * Extended {@link IllegalStateException}.
 *
 * @author Abe White
 * @since 0.4.0
 * @nojavadoc
 */
public class InvalidStateException
    extends IllegalStateException
    implements Serializable, ExceptionInfo {

    private transient boolean _fatal = false;
    private transient Object _failed = null;
    private transient Throwable[] _nested = null;

    public InvalidStateException(Message msg, Throwable[] nested, Object failed,
        boolean fatal) {
        this(msg.getMessage(), nested, failed, fatal);
    }

    public InvalidStateException(String msg, Throwable[] nested, Object failed,
        boolean fatal) {
        super(msg);
        _nested = nested;
        _failed = failed;
        _fatal = fatal;
    }

    public int getType() {
        return USER;
    }

    public int getSubtype() {
        return UserException.INVALID_STATE;
    }

    public boolean isFatal() {
        return _fatal;
    }

    public Throwable getCause() {
        return PersistenceExceptions.getCause(_nested);
    }

    public Throwable[] getNestedThrowables() {
        return (_nested == null) ? Exceptions.EMPTY_THROWABLES : _nested;
    }

    public Object getFailedObject() {
        return _failed;
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
        _nested = (Throwable[]) in.readObject();
    }
}

