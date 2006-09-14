package org.openejb.core.entity;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.SystemException;
import org.openejb.spi.SecurityService;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;
import org.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="bmpContainer"
 */
public class EntityContainer implements org.openejb.RpcContainer, TransactionContainer {

    private EntityInstanceManager instanceManager;

    private Map deploymentRegistry;

    private Object containerID = null;

    public Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public EntityContainer(Object id, TransactionManager transactionManager, SecurityService securityService, Map registry, int poolSize) throws OpenEJBException {
        this.deploymentRegistry = registry;
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new EntityInstanceManager(this, transactionManager, securityService, poolSize);
    }

    public DeploymentInfo [] deployments() {
        return (DeploymentInfo []) deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return (DeploymentInfo) deploymentRegistry.get(deploymentID);
    }

    public int getContainerType() {
        return Container.ENTITY;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException {
        Map registry = new HashMap(deploymentRegistry);
        registry.put(deploymentID, info);
        deploymentRegistry = registry;
        org.openejb.core.CoreDeploymentInfo di = (org.openejb.core.CoreDeploymentInfo) info;
        di.setContainer(this);
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws org.openejb.OpenEJBException {
        try {

            org.openejb.core.CoreDeploymentInfo deployInfo = (org.openejb.core.CoreDeploymentInfo) this.getDeploymentInfo(deployID);

            ThreadContext callContext = ThreadContext.getThreadContext();
            callContext.set(deployInfo, primKey, securityIdentity);

            boolean authorized = getSecurityService().isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));
            if (!authorized)
                throw new org.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            String methodName = callMethod.getName();

            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (declaringClass != EJBHome.class && declaringClass != EJBLocalHome.class) {

                    if (methodName.equals("create")) {

                        return createEJBObject(callMethod, args, callContext);
                    } else if (methodName.startsWith("find")) {

                        return findMethod(callMethod, args, callContext);
                    } else {

                        return homeMethod(callMethod, args, callContext);
                    }
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, args, callContext);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, args, callContext);
                return null;
            }

            callContext.setCurrentOperation(Operations.OP_BUSINESS);
            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);
            Object retValue = invoke(callMethod, runMethod, args, callContext);

            return retValue;

        } finally {
            /*
                The thread context must be stripped from the thread before returning or throwing an exception
                so that an object outside the container does not have access to a
                bean's JNDI ENC.  In addition, its important for the
                org.openejb.core.ivm.java.javaURLContextFactory, which determines the context
                of a JNDI lookup based on the presence of a ThreadContext object.  If no ThreadContext
                object is available, then the request is assumed to be made from outside the container
                system and is given the global OpenEJB JNDI name space instead.  If there is a thread context,
                then the request is assumed to be made from within the container system and so the
                javaContextFactory must return the JNDI ENC of the current enterprise bean which it
                obtains from the DeploymentInfo object associated with the current thread context.
            */
            ThreadContext.setThreadContext(null);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public EntityInstanceManager getInstanceManager() {
        return instanceManager;
    }

    protected Object invoke(Method callMethod, Method runMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, transactionManager);
        txContext.callContext = callContext;

        EntityBean bean = null;
        txPolicy.beforeInvoke(bean, txContext);

        Object returnValue = null;

        try {

            try {
                bean = instanceManager.obtainInstance(callContext);
            } catch (org.openejb.OpenEJBException e) {

                throw e.getRootCause();
            }

            ejbLoad_If_No_Transaction(callContext, bean);
            returnValue = runMethod.invoke(bean, args);
            ejbStore_If_No_Transaction(callContext, bean);
            instanceManager.poolInstance(callContext, bean);
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle enterprise bean exceptions
            if (ite.getTargetException() instanceof RuntimeException) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, bean);
                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (org.openejb.SystemException se) {
            txPolicy.handleSystemException(se.getRootCause(), bean, txContext);
        } catch (Throwable iae) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            txPolicy.handleSystemException(iae, bean, txContext);
        } finally {
            txPolicy.afterInvoke(bean, txContext);
        }

        return returnValue;
    }

    public void ejbLoad_If_No_Transaction(ThreadContext callContext, EntityBean bean)
            throws org.openejb.SystemException, Exception {
        byte orginalOperation = callContext.getCurrentOperation();
        if (orginalOperation == Operations.OP_BUSINESS || orginalOperation == Operations.OP_REMOVE) {

            Transaction currentTx = null;
            try {
                currentTx = getTransactionManager().getTransaction();
            } catch (javax.transaction.SystemException se) {
                throw new org.openejb.SystemException("Transaction Manager failure", se);
            }

            if (currentTx == null) {
                callContext.setCurrentOperation(org.openejb.core.Operations.OP_LOAD);
                try {
                    ((javax.ejb.EntityBean) bean).ejbLoad();
                } catch (Exception e) {

                    instanceManager.discardInstance(callContext, (EntityBean) bean);
                    throw e;
                } finally {
                    callContext.setCurrentOperation(orginalOperation);
                }
            }

        }
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void ejbStore_If_No_Transaction(ThreadContext callContext, EntityBean bean)
            throws Exception {

        byte currentOp = callContext.getCurrentOperation();
        if (currentOp == Operations.OP_BUSINESS) {

            Transaction currentTx = null;
            try {
                currentTx = getTransactionManager().getTransaction();
            } catch (javax.transaction.SystemException se) {
                throw new org.openejb.SystemException("Transaction Manager failure", se);
            }

            if (currentTx == null) {
                callContext.setCurrentOperation(org.openejb.core.Operations.OP_STORE);
                try {
                    ((javax.ejb.EntityBean) bean).ejbStore();
                } catch (Exception e) {

                    instanceManager.discardInstance(callContext, (EntityBean) bean);
                    throw e;
                } finally {
                    callContext.setCurrentOperation(currentOp);
                }
            }
        }
    }

    protected void didCreateBean(ThreadContext callContext, EntityBean bean) throws org.openejb.OpenEJBException {
    }

    protected ProxyInfo createEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {

        org.openejb.core.CoreDeploymentInfo deploymentInfo = (org.openejb.core.CoreDeploymentInfo) callContext.getDeploymentInfo();

        callContext.setCurrentOperation(Operations.OP_CREATE);
        EntityBean bean = null;
        Object primaryKey = null;

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, transactionManager);
        txContext.callContext = callContext;

        /*
        * According to section 9.1.5.1 of the EJB 1.1 specification, the "ejbPostCreate(...) 
        * method executes in the same transaction context as the previous ejbCreate(...) method."
        *
        * For this reason the TransactionScopeHandler methods usally preformed by the invoke( )
        * operation must be handled here along with the call explicitly.
        * This ensures that the afterInvoke() is not processed between the ejbCreate and ejbPostCreate methods to
        * ensure that the ejbPostCreate executes in the same transaction context of the ejbCreate.
        * This would otherwise not be possible if container-managed transactions were used because 
        * the TransactionScopeManager would attempt to commit the transaction immediately after the ejbCreate 
        * and before the ejbPostCreate had a chance to execute.  Once the ejbPostCreate method execute the 
        * super classes afterInvoke( ) method will be executed committing the transaction if its a CMT.
        */

        txPolicy.beforeInvoke(bean, txContext);

        try {

            bean = instanceManager.obtainInstance(callContext);
            Method ejbCreateMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

            primaryKey = ejbCreateMethod.invoke(bean, args);

            callContext.setPrimaryKey(primaryKey);
            didCreateBean(callContext, bean);
            callContext.setCurrentOperation(Operations.OP_POST_CREATE);

            Method ejbPostCreateMethod = deploymentInfo.getMatchingPostCreateMethod(ejbCreateMethod);

            ejbPostCreateMethod.invoke(bean, args);

            primaryKey = callContext.getPrimaryKey();
            callContext.setPrimaryKey(null);
            instanceManager.poolInstance(callContext, bean);
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle enterprise bean exceptions
            if (ite.getTargetException() instanceof RuntimeException) {
                /* System Exception ****************************/
                txPolicy.handleSystemException(ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, bean);
                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (OpenEJBException e) {
            txPolicy.handleSystemException(e.getRootCause(), bean, txContext);
        } catch (Throwable e) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInInitializerError - if the initialization provoked by this method fails.
            */
            txPolicy.handleSystemException(e, bean, txContext);
        } finally {
            txPolicy.afterInvoke(bean, txContext);
        }

        Class callingClass = callMethod.getDeclaringClass();
        Class objectInterface = deploymentInfo.getObjectInterface(callingClass);
        return new ProxyInfo(deploymentInfo, primaryKey, objectInterface, this);

    }

    protected Object findMethod(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {
        org.openejb.core.CoreDeploymentInfo deploymentInfo = (org.openejb.core.CoreDeploymentInfo) callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operations.OP_FIND);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        Object returnValue = invoke(callMethod, runMethod, args, callContext);

        Class callingClass = callMethod.getDeclaringClass();
        Class objectInterface = deploymentInfo.getObjectInterface(callingClass);

        /*
        * Find operations return either a single primary key or a collection of primary keys.
        * The primary keys are converted to ProxyInfo objects.
        */
        if (returnValue instanceof java.util.Collection) {
            java.util.Iterator keys = ((java.util.Collection) returnValue).iterator();
            java.util.Vector proxies = new java.util.Vector();
            while (keys.hasNext()) {
                Object primaryKey = keys.next();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey, objectInterface, this));
            }
            returnValue = proxies;
        } else if (returnValue instanceof java.util.Enumeration) {
            java.util.Enumeration keys = (java.util.Enumeration) returnValue;
            java.util.Vector proxies = new java.util.Vector();
            while (keys.hasMoreElements()) {
                Object primaryKey = keys.nextElement();
                proxies.addElement(new ProxyInfo(deploymentInfo, primaryKey, objectInterface, this));
            }
            returnValue = new org.openejb.util.ArrayEnumeration(proxies);
        } else
            returnValue = new ProxyInfo(deploymentInfo, returnValue, objectInterface, this);

        return returnValue;
    }

    protected Object homeMethod(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {
        org.openejb.core.CoreDeploymentInfo deploymentInfo = (org.openejb.core.CoreDeploymentInfo) callContext.getDeploymentInfo();
        callContext.setCurrentOperation(Operations.OP_HOME);
        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
        return invoke(callMethod, runMethod, args, callContext);
    }

    protected void didRemove(EntityBean bean, ThreadContext callContext) throws OpenEJBException {
    }

    protected void removeEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {
        callContext.setCurrentOperation(Operations.OP_REMOVE);

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, transactionManager);
        txContext.callContext = callContext;

        EntityBean bean = null;
        txPolicy.beforeInvoke(bean, txContext);

        try {

            bean = instanceManager.obtainInstance(callContext);

            ejbLoad_If_No_Transaction(callContext, bean);
            bean.ejbRemove();
            didRemove(bean, callContext);
            instanceManager.poolInstance(callContext, bean);
        } catch (org.openejb.SystemException se) {
            txPolicy.handleSystemException(se.getRootCause(), bean, txContext);
        } catch (Exception e) {// handle reflection exception
            if (e instanceof RuntimeException) {
                /* System Exception ****************************/
                txPolicy.handleSystemException(e, bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext, bean);
                txPolicy.handleApplicationException(e, txContext);
            }
        } finally {
            txPolicy.afterInvoke(bean, txContext);
        }
    }

    public void discardInstance(Object bean, ThreadContext threadContext) {
        if (bean != null) {
            try {
                instanceManager.discardInstance(threadContext, (EntityBean) bean);
            } catch (SystemException e) {
                logger.error("The instance manager encountered an unkown system exception while trying to discard the entity instance with primary key " + threadContext.getPrimaryKey());
            }
        }
    }

}
