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

/**
 * Rollback occurred on attempt to commit.
 *
 * @author Abe White
 * @since 0.4.0
 * @nojavadoc
 */
public class RollbackException
    extends javax.persistence.RollbackException
    implements Serializable, ExceptionInfo {
	
    private transient Object _failed = null;

    private transient Throwable[] _nested;

    public RollbackException(Exception e) {
        super(e.getMessage());
        _nested = new Throwable[]{ e };
    }

    public int getType() {
        return STORE;
    }

    public int getSubtype() {
        return GENERAL;
    }

    public boolean isFatal() {
        return true;
    }

    public Throwable getCause() {
        return PersistenceExceptions.getCause(_nested);
    }

    public Throwable[] getNestedThrowables() {
        return _nested;
    }

    public Object getFailedObject() {
        return _failed;
    }

    public RollbackException setFailedObject(Object failed) {
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
        out.writeObject(Exceptions.replaceNestedThrowables(_nested));
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        _nested = (Throwable[]) in.readObject();
    }
}

