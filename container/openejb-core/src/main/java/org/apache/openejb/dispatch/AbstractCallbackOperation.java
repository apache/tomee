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
package org.apache.openejb.dispatch;

import java.io.Serializable;

import javax.ejb.EnterpriseBean;

import org.apache.geronimo.interceptor.InvocationResult;

import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.timer.TimerState;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractCallbackOperation implements VirtualOperation, Serializable {

    protected InvocationResult invoke(EjbInvocation invocation, EJBOperation operation) throws Throwable {
        EJBInstanceContext ctx = invocation.getEJBInstanceContext();
        boolean oldTimerMethodAvailable = ctx.setTimerState(operation);
        try {
            ctx.setOperation(operation);
            try {
                return invocation.createResult(doOperation(ctx.getInstance(), invocation.getArguments()));
            } catch (Throwable t) {
                if (t instanceof Exception && t instanceof RuntimeException == false) {
                    // checked exception - which we simply include in the result
                    return invocation.createExceptionResult((Exception)t);
                } else {
                    // unchecked Exception - just throw it to indicate an abnormal completion
                    throw t;
                }
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }
    }

    protected abstract Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable;

}
