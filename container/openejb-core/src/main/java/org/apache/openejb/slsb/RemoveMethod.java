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
package org.apache.openejb.slsb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.VirtualOperation;

/**
 * Virtual operation handling removal of an instance.
 *
 * @version $Revision$ $Date$
 */
public class RemoveMethod implements VirtualOperation {
    public static final RemoveMethod INSTANCE = new RemoveMethod();
    private static final InvocationResult NULL_RESULT = new SimpleInvocationResult(true, null);

    public RemoveMethod() {
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();

        // call create
        Object instance = ctx.getInstance();
        Object[] args = invocation.getArguments();
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            Method method = invocation.getEjbDeployment().getBeanClass().getMethod("ejbRemove", null);
            method.invoke(instance, args);
            return NULL_RESULT;
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception)t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
        }
    }
}
