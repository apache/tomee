/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.entity;

import org.openejb.CallbackMethod;
import org.openejb.EjbCallbackInvocation;
import org.openejb.EjbInvocation;
import org.openejb.EJBOperation;
import org.openejb.dispatch.AbstractCallbackOperation;
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
