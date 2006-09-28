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
package org.apache.openejb.sfsb;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.CallbackMethod;
import org.apache.openejb.EjbCallbackInvocation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.dispatch.AbstractCallbackOperation;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @version $Revision$ $Date$
 */
public class StatefulCallbackInterceptor implements Interceptor {
    private static final SetSessionContextOperation SET_SESSION_CONTEXT = new SetSessionContextOperation();
    private static final EJBActivateOperation EJB_ACTIVATE = new EJBActivateOperation();
    private static final EJBPassivateOperation EJB_PASSIVATE = new EJBPassivateOperation();

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbCallbackInvocation ejbCallbackInvocation = (EjbCallbackInvocation) invocation;

        CallbackMethod callbackMethod = ejbCallbackInvocation.getCallbackMethod();
        if (callbackMethod == CallbackMethod.SET_CONTEXT) {
            InvocationResult result = SET_SESSION_CONTEXT.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.ACTIVATE) {
            InvocationResult result = EJB_ACTIVATE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.PASSIVATE) {
            InvocationResult result = EJB_PASSIVATE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.AFTER_BEGIN) {
            InvocationResult result = AfterBegin.INSTANCE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.BEFORE_COMPLETION) {
            InvocationResult result = BeforeCompletion.INSTANCE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.AFTER_COMPLETION) {
            InvocationResult result = AfterCompletion.INSTANCE.execute(ejbCallbackInvocation);
            return result;
        } else {
            throw new AssertionError("Unknown callback method " + callbackMethod);
        }
    }

    private static final class SetSessionContextOperation extends AbstractCallbackOperation {
        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.SETCONTEXT);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((SessionBean)instance).setSessionContext((SessionContext)arguments[0]);
            return null;
        }
    }

    private static final class EJBActivateOperation extends AbstractCallbackOperation {
        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBACTIVATE);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((SessionBean)instance).ejbActivate();
            return null;
        }
    }

    private static final class EJBPassivateOperation extends AbstractCallbackOperation {
        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBACTIVATE);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((SessionBean)instance).ejbPassivate();
            return null;
        }
    }

}
