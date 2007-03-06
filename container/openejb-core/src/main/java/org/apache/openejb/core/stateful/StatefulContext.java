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
package org.apache.openejb.core.stateful;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.core.BaseSessionContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;

import java.security.Principal;


/**
 * @version $Rev$ $Date$
 */
public class StatefulContext extends BaseSessionContext {

    protected final static State[] states = new State[Operation.values().length];

    public StatefulContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public StatefulContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    protected State getState() {
        Operation operation = ThreadContext.getThreadContext().getCurrentOperation();
        State state = states[operation.ordinal()];

        if (state == null) throw new IllegalArgumentException("Invalid operation " + operation + " for this context");

        return state;
    }

    static {
        states[Operation.INJECTION.ordinal()] = new InjectionStatelessState();
        states[Operation.LIFECYCLE.ordinal()] = new LifecycleStatelessState();
        states[Operation.BUSINESS.ordinal()] = new BusinessStatelessState();
        states[Operation.AFTER_BEGIN.ordinal()] = new BeforeCompletion();
        states[Operation.BEFORE_COMPLETION.ordinal()] = new BeforeCompletion();
        states[Operation.AFTER_COMPLETION.ordinal()] = new AfterCompletion();
        states[Operation.TIMEOUT.ordinal()] = new TimeoutStatelessState();
    }

    /**
     * afterBegin
     * beforeCompletion
     */
    public static class BeforeCompletion extends StatelessState {

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }
    }

    /**
     * afterCompletion
     */
    public static class AfterCompletion extends StatelessState {
        public MessageContext getMessageContext() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public Class getInvokedBusinessInterface() {
            throw new IllegalStateException();
        }

        public Principal getCallerPrincipal(SecurityService securityService) {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isJNDIAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerFactoryAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return false;
        }
    }
}
