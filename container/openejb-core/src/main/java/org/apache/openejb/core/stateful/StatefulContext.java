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

import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.spi.SecurityService;

import javax.transaction.TransactionManager;
import javax.xml.rpc.handler.MessageContext;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class StatefulContext extends org.apache.openejb.core.CoreContext implements javax.ejb.SessionContext {

    public StatefulContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public void checkBeanState(byte methodCategory) throws IllegalStateException {
        /*
        The methodCategory will be one of the following constants.

        SECURITY_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        EJBHOME_METHOD
        USER_TRANSACTION_METHOD:

        The super class, CoreContext determines if Context.getUserTransaction( ) method
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.

        The USER_TRANSACTION_METHOD will never be passed as a methodCategory in the SessionSynchronization
        interface methods. The CoreContext won't allow it.

        */
        ThreadContext callContext = ThreadContext.getThreadContext();

        switch (callContext.getCurrentOperation()) {
            case OP_SET_CONTEXT:
                /*
                Allowed Operations:
                    getEJBHome
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                */
                if (methodCategory != EJBHOME_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;
            case OP_CREATE:
            case OP_REMOVE:
            case OP_ACTIVATE:
            case OP_PASSIVATE:
            case OP_AFTER_COMPLETION:
                /*
                Allowed Operations:
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction (not allowed in OP_AFTER_COMPLETION)
                Prohibited Operations:
                    getRollbackOnly,
                    setRollbackOnly
                */
                if (methodCategory == ROLLBACK_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                else
                    break;
            case OP_BUSINESS:
            case OP_AFTER_BEGIN:
            case OP_BEFORE_COMPLETION:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    isCallerInRole
                    getEJBObject
                    getPrimaryKey
                    getRollbackOnly,
                    setRollbackOnly
                    getUserTransaction (business methods only)
                Prohibited Operations:
                */
                break;
        }

    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID, InterfaceType interfaceType) {
        return new StatefulEjbObjectHandler(container, pk, depID, interfaceType);
    }

    public MessageContext getMessageContext() {
        throw new UnsupportedOperationException("not implemented");
    }

    public Object getBusinessObject(Class businessInterface) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Class getInvokedBusinessInterface() {
        throw new UnsupportedOperationException("not implemented");
    }

    private Object writeReplace() throws ObjectStreamException {
        return new A();
    }

    private static class A implements Serializable {
        private Object readResolve() throws ObjectStreamException {
            // DMB: Could easily be done generically with an recipie
            TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            return new StatefulContext(transactionManager, securityService);
        }
    }
}