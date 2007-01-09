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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

public class NamingException extends javax.naming.NamingException {
    private org.apache.openejb.OpenEJBException delegate;

    public NamingException(String message, org.apache.openejb.OpenEJBException delegateArg) {
        super();
        delegate = delegateArg;
    }

    public NamingException(String message, Throwable rootCause) {
        super();
        delegate = new org.apache.openejb.OpenEJBException(message, rootCause);
    }

    public String getMessage() {
        return delegate.getMessage();
    }

    public void printStackTrace() {
        delegate.printStackTrace();
    }

    public void printStackTrace(java.io.PrintStream stream) {
        delegate.printStackTrace(stream);
    }

    public void printStackTrace(java.io.PrintWriter writer) {
        delegate.printStackTrace(writer);
    }
}
