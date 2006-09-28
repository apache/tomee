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

package org.apache.openejb.naming;

import javax.naming.Context;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.ExtendedEjbDeployment;

/**
 * An interceptor that pushes the current component's java:comp context into
 * the java: JNDI namespace
 *
 * @version $Rev$ $Date$
 */
public class ComponentContextInterceptor implements Interceptor {
    private final Interceptor next;

    public ComponentContextInterceptor(Interceptor next) {
        assert next != null;
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        ExtendedEjbDeployment deployment = (ExtendedEjbDeployment) ejbInvocation.getEjbDeployment();
        Context componentContext = deployment.getComponentContext();

        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);
            return next.invoke(invocation);
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
