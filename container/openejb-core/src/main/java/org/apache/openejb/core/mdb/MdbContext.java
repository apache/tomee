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
import org.apache.openejb.spi.SecurityService;


/**
 * @version $Rev$ $Date$
 */
public class MdbContext extends BaseContext implements MessageDrivenContext {

    public MdbContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    protected MdbContext(TransactionManager transactionManager, SecurityService securityService, UserTransaction userTransaction) {
        super(transactionManager, securityService, userTransaction);
    }

    protected void init() {
        states[Operation.INJECTION.ordinal()] = INJECTION;
        states[Operation.LIFECYCLE.ordinal()] = LIFECYCLE;
        states[Operation.BUSINESS.ordinal()] = BUSINESS_TIMEOUT;
        states[Operation.TIMEOUT.ordinal()] = BUSINESS_TIMEOUT;
    }

    /**
     * Dependency injection methods (e.g., setMessageDrivenContext)
     */
    protected final State INJECTION = new State() {
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        public Principal getCallerPrincipal() {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(String roleName) {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public void setRollbackOnly() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean getRollbackOnly() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public TimerService getTimerService() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isResourceManagerAccessAllowed() {
            return false;
        }

        public boolean isEnterpriseBeanAccessAllowed() {
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
    };

    /**
     * PostConstruct, Pre-Destroy lifecycle callback interceptor methods
     */
    protected final State LIFECYCLE = new State() {
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        public Principal getCallerPrincipal() {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(String roleName) {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public void setRollbackOnly() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean getRollbackOnly() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }

        public boolean isMessageContextAccessAllowed() {
            return false;
        }

        public boolean isResourceManagerAccessAllowed() {
            return false;
        }

        public boolean isEnterpriseBeanAccessAllowed() {
            return false;
        }

        public boolean isEntityManagerAccessAllowed() {
            return false;
        }

        public boolean isTimerAccessAllowed() {
            return super.isTimerAccessAllowed();    //todo: consider this autogenerated code
        }
    };

    /**
     * Message listener method, business method interceptor method
     * and imeout callback method
     */
    protected final State BUSINESS_TIMEOUT = new State() {
        public EJBHome getEJBHome() {
            throw new IllegalStateException();
        }

        public EJBLocalHome getEJBLocalHome() {
            throw new IllegalStateException();
        }

        public boolean isCallerInRole(String roleName) {
            throw new IllegalStateException();
        }

        public UserTransaction getUserTransaction() throws IllegalStateException {
            throw new IllegalStateException();
        }

        public boolean isUserTransactionAccessAllowed() {
            return false;
        }
    };
}
