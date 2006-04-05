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
package org.openejb.sfsb;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.openejb.CallbackMethod;
import org.openejb.EjbCallbackInvocation;
import org.openejb.EjbInvocation;
import org.openejb.EJBOperation;
import org.openejb.dispatch.AbstractCallbackOperation;

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
