package org.openejb.core.stateful;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
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
 * @org.apache.xbean.XBean element="statefulContainer"
 */
public class StatefulContainer implements org.openejb.RpcContainer, TransactionContainer {

    private StatefulInstanceManager instanceManager;

    private HashMap deploymentRegistry;

    private Object containerID = null;

    private Method EJB_REMOVE_METHOD = null;

    final static protected Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatefulContainer(Object id, TransactionManager transactionManager, SecurityService securityService, HashMap registry, Class passivator, int timeOut, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.deploymentRegistry = registry;
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new StatefulInstanceManager(transactionManager, securityService, passivator, timeOut, poolSize, bulkPassivate);

        try {
            EJB_REMOVE_METHOD = javax.ejb.SessionBean.class.getMethod("ejbRemove", new Class [0]);
        } catch (NoSuchMethodException nse) {
            throw new SystemException("Fixed remove method can not be initated", nse);
        }
    }

    public DeploymentInfo [] deployments() {
        return (DeploymentInfo []) deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return (DeploymentInfo) deploymentRegistry.get(deploymentID);
    }

    public int getContainerType() {
        return Container.STATEFUL;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException {
        HashMap registry = (HashMap) deploymentRegistry.clone();
        registry.put(deploymentID, info);
        deploymentRegistry = registry;
        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo) info;
        di.setContainer(this);
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws org.openejb.OpenEJBException {
        try {

            org.openejb.core.DeploymentInfo deployInfo = (org.openejb.core.DeploymentInfo) this.getDeploymentInfo(deployID);

            ThreadContext callContext = ThreadContext.getThreadContext();
            callContext.set(deployInfo, primKey, securityIdentity);

            boolean authorized = getSecurityService().isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));
            if (!authorized)
                throw new org.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            String methodName = callMethod.getName();

            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (methodName.equals("create")) {
                    return createEJBObject(callMethod, args, callContext);
                } else if (methodName.equals("remove")) {
                    removeEJBObject(callMethod, args, callContext);
                    return null;
                }
            } else if ((EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) && methodName.equals("remove")) {
                removeEJBObject(callMethod, args, callContext);
                return null;
            }

            SessionBean bean = null;

            bean = instanceManager.obtainInstance(primKey, callContext);
            callContext.setCurrentOperation(Operations.OP_BUSINESS);
            Object returnValue = null;
            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            returnValue = this.invoke(callMethod, runMethod, args, bean, callContext);

            instanceManager.poolInstance(primKey, bean);

            return deployInfo.convertIfLocalReference(callMethod, returnValue);

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

    protected Object invoke(Method callMethod, Method runMethod, Object [] args, EnterpriseBean bean, ThreadContext callContext)
            throws org.openejb.OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, getTransactionManager());
        txContext.context.put(StatefulInstanceManager.class, instanceManager);
        try {
            txPolicy.beforeInvoke(bean, txContext);
        } catch (org.openejb.ApplicationException e) {
            if (e.getRootCause() instanceof javax.transaction.TransactionRequiredException ||
                    e.getRootCause() instanceof java.rmi.RemoteException) {

                instanceManager.poolInstance(callContext.getPrimaryKey(), bean);
            }
            throw e;
        }

        Object returnValue = null;
        try {
            returnValue = runMethod.invoke(bean, args);
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle enterprise bean exception
            if (ite.getTargetException() instanceof RuntimeException) {
                /* System Exception ****************************/

                txPolicy.handleSystemException(ite.getTargetException(), bean, txContext);
            } else {
                /* Application Exception ***********************/
                instanceManager.poolInstance(callContext.getPrimaryKey(), bean);

                txPolicy.handleApplicationException(ite.getTargetException(), txContext);
            }
        } catch (Throwable re) {// handle reflection exception
            /*
              Any exception thrown by reflection; not by the enterprise bean. Possible
              Exceptions are:
                IllegalAccessException - if the underlying method is inaccessible.
                IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                NullPointerException - if the specified object is null and the method is an instance method.
                ExceptionInitializerError - if the initialization provoked by this method fails.
            */

            txPolicy.handleSystemException(re, bean, txContext);

        } finally {

            txPolicy.afterInvoke(bean, txContext);
        }

        return returnValue;
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public StatefulInstanceManager getInstanceManager() {
        return instanceManager;
    }

    protected void removeEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {

        try {
            EnterpriseBean bean = instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);
            if (bean != null) {

                callContext.setCurrentOperation(Operations.OP_REMOVE);
                invoke(callMethod, this.EJB_REMOVE_METHOD, null, bean, callContext);
            }
        } finally {
            instanceManager.freeInstance(callContext.getPrimaryKey());
        }

    }

    protected ProxyInfo createEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.openejb.OpenEJBException {
        org.openejb.core.DeploymentInfo deploymentInfo = (org.openejb.core.DeploymentInfo) callContext.getDeploymentInfo();
        Class beanType = deploymentInfo.getBeanClass();
        Object primaryKey = this.newPrimaryKey();
        callContext.setPrimaryKey(primaryKey);

        EnterpriseBean bean = instanceManager.newInstance(primaryKey, beanType);

        Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);

        callContext.setCurrentOperation(Operations.OP_CREATE);
        invoke(callMethod, runMethod, args, bean, callContext);

        instanceManager.poolInstance(primaryKey, bean);

        Class callingClass = callMethod.getDeclaringClass();
        boolean isLocalInterface = EJBLocalHome.class.isAssignableFrom(callingClass);
        return new ProxyInfo(deploymentInfo, primaryKey, isLocalInterface, this);
    }

    protected Object newPrimaryKey() {
        return new java.rmi.dgc.VMID();
    }

    public void discardInstance(Object bean, ThreadContext threadContext) {
        try {
            Object primaryKey = threadContext.getPrimaryKey();
            instanceManager.freeInstance(primaryKey);
        } catch (Throwable t) {
            logger.error("", t);
        }
    }
}
