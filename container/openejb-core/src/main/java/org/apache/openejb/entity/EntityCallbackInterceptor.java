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
package org.apache.openejb.entity;

import org.apache.openejb.CallbackMethod;
import org.apache.openejb.EjbCallbackInvocation;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.dispatch.AbstractCallbackOperation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.Interceptor;

import javax.ejb.EnterpriseBean;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

/**
 * @version $Revision$ $Date$
 */
public class EntityCallbackInterceptor implements Interceptor {

    private static final SetEntityContextOperation SET_ENTITY_CONTEXT = new SetEntityContextOperation();
    private static final UnsetEntityContextOperation UNSET_ENTITY_CONTEXT = new UnsetEntityContextOperation();
    private static final EJBActivateOperation EJB_ACTIVATE = new EJBActivateOperation();
    private static final EJBPassivateOperation EJB_PASSIVATE = new EJBPassivateOperation();
    private static final EJBLoadOperation EJB_LOAD = new EJBLoadOperation();
    private static final EJBStoreOperation EJB_STORE = new EJBStoreOperation();

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbCallbackInvocation ejbCallbackInvocation = (EjbCallbackInvocation) invocation;

        CallbackMethod callbackMethod = ejbCallbackInvocation.getCallbackMethod();
        if (callbackMethod == CallbackMethod.SET_CONTEXT) {
            InvocationResult result = SET_ENTITY_CONTEXT.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.UNSET_CONTEXT) {
            InvocationResult result = UNSET_ENTITY_CONTEXT.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.ACTIVATE) {
            InvocationResult result = EJB_ACTIVATE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.PASSIVATE) {
            InvocationResult result = EJB_PASSIVATE.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.LOAD) {
            InvocationResult result = EJB_LOAD.execute(ejbCallbackInvocation);
            return result;
        } else if (callbackMethod == CallbackMethod.STORE) {
            InvocationResult result = EJB_STORE.execute(ejbCallbackInvocation);
            return result;
        } else {
            throw new AssertionError("Unknown callback method " + callbackMethod);
        }
    }

    private static final class EJBActivateOperation extends AbstractCallbackOperation {

        private EJBActivateOperation() {}

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBACTIVATE);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).ejbActivate();
            return null;
        }
    }

    private static final class EJBLoadOperation extends AbstractCallbackOperation {

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBLOAD);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).ejbLoad();
            return null;
        }

    }

    private static final class EJBPassivateOperation extends AbstractCallbackOperation {

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBACTIVATE);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).ejbPassivate();
            return null;
        }

    }

    private static final class EJBStoreOperation extends AbstractCallbackOperation {

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.EJBLOAD);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).ejbStore();
            return null;
        }

    }

    private static final class SetEntityContextOperation extends AbstractCallbackOperation {

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.SETCONTEXT);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).setEntityContext((EntityContext)arguments[0]);
            return null;
        }

    }

    private static final class UnsetEntityContextOperation extends AbstractCallbackOperation {

        public InvocationResult execute(EjbInvocation invocation) throws Throwable {
            return invoke(invocation, EJBOperation.SETCONTEXT);
        }

        protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
            ((EntityBean)instance).unsetEntityContext();
            return null;
        }
    }
}
