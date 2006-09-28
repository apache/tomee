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

import java.security.Principal;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.security.auth.Subject;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.timer.TimerState;

/**
 * Implementation of SessionContext using the State pattern to determine
 * which methods can be called given the current state of the Session bean.
 *
 * @version $Revision$ $Date$
 */
public class StatefulSessionContext extends EJBContextImpl implements SessionContext {
    public StatefulSessionContext(StatefulInstanceContext context, TransactionManager transactionManager, UserTransaction userTransaction) {
        super(context, transactionManager, userTransaction);
        state = INACTIVE;
    }

    void setState(EJBOperation operation) {
        state = states[operation.getOrdinal()];
        assert (state != null) : "Invalid EJBOperation for Stateful SessionBean, ordinal=" + operation;

        context.setTimerServiceAvailable(timerServiceAvailable[operation.getOrdinal()]);
    }

    public boolean setTimerState(EJBOperation operation) {
        boolean oldTimerState = TimerState.getTimerState();
        TimerState.setTimerState(timerMethodsAvailable[operation.getOrdinal()]);
        return oldTimerState;
    }

    public MessageContext getMessageContext() throws IllegalStateException {
        return ((StatefulSessionContextState) state).getMessageContext();
    }

    public abstract static class StatefulSessionContextState extends EJBContextState {
        protected MessageContext getMessageContext() {
            throw new UnsupportedOperationException();
        }
    }

    public static final StatefulSessionContextState INACTIVE = new StatefulSessionContextState() {
        public EJBHome getEJBHome(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBHome() cannot be called when inactive");
        }

        public EJBLocalHome getEJBLocalHome(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalHome() cannot be called when inactive");
        }

        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called when inactive");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called when inactive");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called when inactive");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called when inactive");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            throw new IllegalStateException("getUserTransaction() cannot be called when inactive");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called when inactive");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called when inactive");
        }

        public MessageContext getMessageContext() {
            throw new IllegalStateException("getMessageContext() cannot be called when inactive");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called when inactive");
        }
    };

    public static final StatefulSessionContextState SETSESSIONCONTEXT = new StatefulSessionContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from setSessionContext(SessionContext)");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from setSessionContext(SessionContext)");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called from setSessionContext(SessionContext)");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called from setSessionContext(SessionContext)");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            throw new IllegalStateException("getUserTransaction() cannot be called from setSessionContext(SessionContext)");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from setSessionContext(SessionContext)");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from setSessionContext(SessionContext)");
        }

        public MessageContext getMessageContext() {
            throw new IllegalStateException("getMessageContext() cannot be called from setSessionContext(SessionContext)");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from from a StatefulSessionBean");
        }
    };

    public static final StatefulSessionContextState EJBCREATEREMOVEACTIVATE = new StatefulSessionContextState() {
        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from ejbCreate/ejbRemove");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from ejbCreate/ejbRemove");
        }

        public MessageContext getMessageContext() {
            throw new IllegalStateException("getMessageContext() cannot be called from ejbCreate/ejbRemove");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from from a StatefulSessionBean");
        }
    };

    public static final StatefulSessionContextState BIZ_INTERFACE = new StatefulSessionContextState() {
        public MessageContext getMessageContext() {
            throw new IllegalStateException("getMessageContext() cannot be called in a business method invocation from component interface)");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from from a StatefulSessionBean");
        }
    };

    private static final StatefulSessionContextState states[] = new StatefulSessionContextState[EJBOperation.MAX_ORDINAL];

    static {
        states[EJBOperation.INACTIVE.getOrdinal()] = INACTIVE;
        states[EJBOperation.SETCONTEXT.getOrdinal()] = SETSESSIONCONTEXT;
        states[EJBOperation.EJBCREATE.getOrdinal()] = EJBCREATEREMOVEACTIVATE;
        states[EJBOperation.EJBREMOVE.getOrdinal()] = EJBCREATEREMOVEACTIVATE;
        states[EJBOperation.EJBACTIVATE.getOrdinal()] = EJBCREATEREMOVEACTIVATE;
        states[EJBOperation.BIZMETHOD.getOrdinal()] = BIZ_INTERFACE;
    }

    private static final boolean timerServiceAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];
    //timer service is never available in sfsb

    private static final boolean timerMethodsAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];

    static {
        timerMethodsAvailable[EJBOperation.BIZMETHOD.getOrdinal()] = true;
        //TODO INCOMPLETE, for session synchronization allowed for afterBegin and beforeCompletion, disallowed afterCompletion.
    }

    public Object lookup(String name){
        //TODO: EJB 3
        throw new UnsupportedOperationException("lookup");
    }

    public Object getBusinessObject(Class businessInterface) {
        //TODO: EJB 3
        throw new UnsupportedOperationException("not implemented");
    }

    public Class getInvokedBusinessInterface() {
        //TODO: EJB 3
        throw new UnsupportedOperationException("not implemented");
    }
    
}
