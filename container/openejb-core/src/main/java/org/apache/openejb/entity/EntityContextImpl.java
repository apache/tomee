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

import java.security.Principal;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityContext;
import javax.ejb.TimerService;
import javax.security.auth.Subject;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;

import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.timer.TimerState;

/**
 * @version $Revision$ $Date$
 */
public class EntityContextImpl extends EJBContextImpl implements EntityContext {
    public EntityContextImpl(EntityInstanceContext context, TransactionManager transactionManager) {
        super(context, transactionManager, null);
    }

    public void setState(EJBOperation operation) {
        state = states[operation.getOrdinal()];
        assert (state != null) : "Invalid EJBOperation for EntityBean, ordinal=" + operation.getOrdinal();
        context.setTimerServiceAvailable(timerServiceAvailable[operation.getOrdinal()]);
    }

    public boolean setTimerState(EJBOperation operation) {
        boolean oldTimerState = TimerState.getTimerState();
        TimerState.setTimerState(timerMethodsAvailable[operation.getOrdinal()]);
        return oldTimerState;
    }

    public Object getPrimaryKey() throws IllegalStateException {
        return ((EntityContextState) state).getPrimaryKey(context);
    }

    public UserTransaction getUserTransaction() throws IllegalStateException {
        throw new IllegalStateException("getUserTransaction is not supported for EntityBean");
    }

    public static abstract class EntityContextState extends EJBContextState {
        public Object getPrimaryKey(EJBInstanceContext context) {
            return context.getId();
        }
    }

    public static EntityContextState INACTIVE = new EntityContextState() {
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

        public Object getPrimaryKey(EJBInstanceContext context) {
            throw new IllegalStateException("getPrimaryKey() cannot be called when inactive");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called when inactive");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called when inactive");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            throw new IllegalStateException("getUserTransaction() is not allowed on an Entity bean");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called when inactive");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called when inactive");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called when inactive");
        }
    };

    public static EntityContextState SETENTITYCONTEXT = new EntityContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from set/unsetEntityContext");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from set/unsetEntityContext");
        }

        public Object getPrimaryKey(EJBInstanceContext context) {
            throw new IllegalStateException("getPrimaryKey() cannot be called from set/unsetEntityContext");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called from set/unsetEntityContext");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called from set/unsetEntityContext");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from set/unsetEntityContext");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from set/unsetEntityContext");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from set/unsetEntityContext");
        }
    };

    public static EntityContextState EJBCREATE = new EntityContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from ejbHome");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from ejbHome");
        }

        public Object getPrimaryKey(EJBInstanceContext context) {
            throw new IllegalStateException("getPrimaryKey() cannot be called from ejbHome");
        }
    };

    public static EntityContextState EJBPOSTCREATE = new EntityContextState() {
    };

    public static EntityContextState EJBREMOVE = new EntityContextState() {
    };

    public static EntityContextState EJBFIND = new EntityContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from ejbFind");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from ejbFind");
        }

        public Object getPrimaryKey(EJBInstanceContext context) {
            throw new IllegalStateException("getPrimaryKey() cannot be called from ejbFind");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from ejbFind");
        }
    };

    public static EntityContextState EJBHOME = new EntityContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from ejbHome");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from ejbHome");
        }

        public Object getPrimaryKey(EJBInstanceContext context) {
            throw new IllegalStateException("getPrimaryKey() cannot be called from ejbHome");
        }
    };

    public static EntityContextState EJBACTIVATE = new EntityContextState() {
        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called from ejbActivate/ejbPassivate");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called from ejbActivate/ejbPassivate");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from ejbActivate/ejbPassivate");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionManager transactionManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from ejbActivate/ejbPassivate");
        }
    };

    public static EntityContextState EJBLOAD = new EntityContextState() {
    };

    public static EntityContextState BIZ_INTERFACE = new EntityContextState() {
    };

    public static EntityContextState EJBTIMEOUT = new EntityContextState() {
    };

    private static final EntityContextState states[] = new EntityContextState[EJBOperation.MAX_ORDINAL];

    static {
        states[EJBOperation.INACTIVE.getOrdinal()] = INACTIVE;
        states[EJBOperation.SETCONTEXT.getOrdinal()] = SETENTITYCONTEXT;
        states[EJBOperation.EJBCREATE.getOrdinal()] = EJBCREATE;
        states[EJBOperation.EJBPOSTCREATE.getOrdinal()] = EJBPOSTCREATE;
        states[EJBOperation.EJBREMOVE.getOrdinal()] = EJBREMOVE;
        states[EJBOperation.EJBFIND.getOrdinal()] = EJBFIND;
        states[EJBOperation.EJBHOME.getOrdinal()] = EJBHOME;
        states[EJBOperation.EJBACTIVATE.getOrdinal()] = EJBACTIVATE;
        states[EJBOperation.EJBLOAD.getOrdinal()] = EJBLOAD;
        states[EJBOperation.BIZMETHOD.getOrdinal()] = BIZ_INTERFACE;
        states[EJBOperation.TIMEOUT.getOrdinal()] = EJBTIMEOUT;
    }

    private static final boolean timerServiceAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];

    static {
        timerServiceAvailable[EJBOperation.EJBCREATE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBPOSTCREATE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBREMOVE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBFIND.getOrdinal()] = true;//TODO ??? don't know
        timerServiceAvailable[EJBOperation.EJBHOME.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBACTIVATE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBLOAD.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.BIZMETHOD.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.TIMEOUT.getOrdinal()] = true;
    }

    private static final boolean timerMethodsAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];

    static {
        timerMethodsAvailable[EJBOperation.EJBPOSTCREATE.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.EJBREMOVE.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.EJBLOAD.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.BIZMETHOD.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.TIMEOUT.getOrdinal()] = true;
    }

    public Object lookup(String name){
        //TODO: EJB 3
        throw new UnsupportedOperationException("lookup");
    }
}
