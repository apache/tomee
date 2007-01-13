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

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.security.ContextManager;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.EjbInvocation;


/**
 * A simple interceptor that sets up the next caller to be the configured run-as
 * <code>Subject</code>.
 *
 * @version $Revision$ $Date$
 */
public final class EjbRunAsInterceptor implements Interceptor {
    private final Interceptor next;

    public EjbRunAsInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = ((EjbInvocation) invocation);
        ExtendedEjbDeployment deployment = (ExtendedEjbDeployment) ejbInvocation.getEjbDeployment();
        Subject runAsSubject = deployment.getRunAsSubject();

        Subject save = ContextManager.getNextCaller();
        try {
            ContextManager.setNextCaller(runAsSubject);
            return next.invoke(invocation);
        } finally {
            ContextManager.setNextCaller(save);
        }
    }
}
