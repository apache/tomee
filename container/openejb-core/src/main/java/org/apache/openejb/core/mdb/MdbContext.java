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
package org.apache.openejb.core.mdb;

import java.security.Principal;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TimerService;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;


/**
 * @version $Rev$ $Date$
 */
public class MdbContext extends BaseContext implements MessageDrivenContext {

    protected final static State[] states = new State[Operation.values().length];

    public MdbContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    protected MdbContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    protected State getState() {
        Operation operation = ThreadContext.getThreadContext().getCurrentOperation();
        State state = states[operation.ordinal()];

        if (state == null) throw new IllegalArgumentException("Invalid operation " + operation + " for this context");

        return state;
    }

    /**
     * Dependency injection methods (e.g., setMessageDrivenContext)
     */
    protected static class InjectionMdbState extends State {
        @Override
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        @Override
        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        @Override
        public Principal getCallerPrincipal(SecurityService securityService) {
            throw new IllegalStateException();
        }

        @Override
        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

        @Override
        public UserTransaction getUserTransaction(UserTransaction userTransaction) throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public TimerService getTimerService() throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        @Override
        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        @Override
        public boolean isEntityManagerFactoryAccessAllowed() {
            return false;
        }

        @Override
        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        @Override
        public boolean isTimerAccessAllowed() {
            return false;
        }
    }

    /**
     * PostConstruct, Pre-Destroy lifecycle callback interceptor methods
     */
    protected static class LifecycleMdbState extends State {
        @Override
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        @Override
        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        @Override
        public Principal getCallerPrincipal(SecurityService securityService) {
            throw new IllegalStateException();
        }

        @Override
        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

        @Override
        public boolean getRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public void setRollbackOnly(TransactionManager transactionManager) throws IllegalStateException {
            throw new IllegalStateException();
        }

        @Override
        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        @Override
        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        @Override
        public boolean isTimerAccessAllowed() {
            return super.isTimerAccessAllowed();    //todo: consider this autogenerated code
        }
    }

    /**
     * Message listener method, business method interceptor method
     * and imeout callback method
     */
    protected static class BusinessTimeoutMdbState extends State {
        @Override
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        @Override
        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        @Override
        public boolean isCallerInRole(SecurityService securityService, String roleName) {
            throw new IllegalStateException();
        }

    }

    static {
        states[Operation.INJECTION.ordinal()] = new InjectionMdbState();
        states[Operation.LIFECYCLE.ordinal()] = new LifecycleMdbState();
        states[Operation.BUSINESS.ordinal()] = new BusinessTimeoutMdbState();
        states[Operation.TIMEOUT.ordinal()] = new BusinessTimeoutMdbState();
    }

}
