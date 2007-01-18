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
package org.apache.openejb.core.entity;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;

import javax.transaction.TransactionManager;

public class OldEntityContext extends org.apache.openejb.core.CoreContext implements javax.ejb.EntityContext {

    public OldEntityContext(TransactionManager transactionManager, SecurityService securityService) {
        super(transactionManager, securityService);
    }

    public void checkBeanState(byte methodCategory) throws IllegalStateException {
        /*
        The methodCategory will be one of the following constants.

        SECURITY_METHOD:
        ROLLBACK_METHOD:
        EJBOBJECT_METHOD:
        EJBHOME_METHOD

        The super class, CoreContext determines if Context.getUserTransaction( ) method
        maybe called before invoking this.checkBeanState( ).  Only "bean managed" transaction
        beans may access this method.

        The USER_TRANSACTION_METHOD constant will never be a methodCategory
        because entity beans are not allowed to have "bean managed" transactions.

        USER_TRANSACTION_METHOD:
        */

        ThreadContext callContext = ThreadContext.getThreadContext();
        org.apache.openejb.DeploymentInfo di = callContext.getDeploymentInfo();

        switch (callContext.getCurrentOperation()) {
            case SET_CONTEXT:
            case UNSET_CONTEXT:
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
            case CREATE:
            case FIND:
            case HOME:
                /*
                Allowed Operations:
                    getEJBHome
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                Prohibited Operations:
                    getEJBObject
                    getPrimaryKey
                    getUserTransaction
                */
                if (methodCategory == EJBOBJECT_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;
            case ACTIVATE:
            case PASSIVATE:
                /*
                Allowed Operations:
                    getEJBHome
                    getEJBObject
                    getPrimaryKey
                Prohibited Operations:
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getUserTransaction
                */
                if (methodCategory != EJBOBJECT_METHOD && methodCategory != EJBHOME_METHOD)
                    throw new IllegalStateException("Invalid operation attempted");
                break;

            case POST_CREATE:
            case REMOVE:
            case LOAD:
            case STORE:
                /* 
                Allowed Operations: 
                    getEJBHome
                    getCallerPrincipal
                    getRollbackOnly,
                    isCallerInRole
                    setRollbackOnly
                    getEJBObject
                    getPrimaryKey
                Prohibited Operations:
                    getUserTransaction
                */
                break;

        }

    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID, InterfaceType interfaceType) {
        return new EntityEjbObjectHandler(container, pk, depID, interfaceType);
    }

}