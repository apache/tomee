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
package org.apache.openejb.security;

import javax.security.auth.Subject;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.security.ContextManager;


/**
 * An interceptor that invokes the bean under the caller's <code>Subject</code>.
 * This allows control over what the bean's code can access via the standard
 * Java Policy implementations.
 * @version $Revision$ $Date$
 */
public final class EJBIdentityInterceptor implements Interceptor {
    private final Interceptor next;

    public EJBIdentityInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        Subject subject = ContextManager.getCurrentCaller();

        try {
            return (InvocationResult) Subject.doAs(subject, new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    try {
                        return next.invoke(invocation);
                    } catch (Exception e) {
                        throw e;
                    } catch (Error e) {
                        throw e;
                    } catch (Throwable t) {
                        throw (AssertionError) new AssertionError("Unexpected Throwable").initCause(t);
                    }
                }
            });
        } catch (PrivilegedActionException pae) {
            Throwable t = pae.getException();
            throw t;
        }
    }
}
