package org.apache.openejb.core.entity;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.core.Operations;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;

import javax.transaction.TransactionManager;

public class EntityContext extends org.apache.openejb.core.CoreContext implements javax.ejb.EntityContext {

    public EntityContext(TransactionManager transactionManager, SecurityService securityService) {
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
            case Operations.OP_SET_CONTEXT:
            case Operations.OP_UNSET_CONTEXT:
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
            case Operations.OP_CREATE:
            case Operations.OP_FIND:
            case Operations.OP_HOME:
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
            case Operations.OP_ACTIVATE:
            case Operations.OP_PASSIVATE:
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

            case Operations.OP_POST_CREATE:
            case Operations.OP_REMOVE:
            case Operations.OP_LOAD:
            case Operations.OP_STORE:
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

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new EntityEjbObjectHandler(container, pk, depID);
    }

}