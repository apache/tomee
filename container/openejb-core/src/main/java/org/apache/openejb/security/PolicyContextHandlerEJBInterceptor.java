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

import javax.security.jacc.PolicyContext;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EjbInvocation;


/**
 * A simple interceptor that sets up the <code>PolicyContextHandlerEnterpriseBean</code>
 * and <code>PolicyContextHandlerEJBArguments</code> so that it can return the
 * proper EJB information to the policy provider.  This code is placed in a
 * seperate interceptor as a optimization for those policy providers that do
 * not need such fine grained control over method invocations.
 *
 * @version $Revision$ $Date$
 * @see org.apache.openejb.security.PolicyContextHandlerEnterpriseBean
 * @see org.apache.openejb.security.PolicyContextHandlerEJBArguments
 * @see EjbSecurityInterceptor
 */
public class PolicyContextHandlerEJBInterceptor implements Interceptor {
    private final Interceptor next;

    public PolicyContextHandlerEJBInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        PolicyContextHandlerDataEJB data = new PolicyContextHandlerDataEJB();

        EjbInvocation ejbInvocation = (EjbInvocation) invocation;

        data.arguments = ejbInvocation.getArguments();
        data.bean = ejbInvocation.getEJBInstanceContext().getInstance();

        PolicyContext.setHandlerData(data);

        try {
            return next.invoke(invocation);
        } finally {
            PolicyContext.setHandlerData(null);
        }
    }
}
