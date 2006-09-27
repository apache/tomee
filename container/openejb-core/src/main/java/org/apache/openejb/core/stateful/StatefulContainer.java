package org.apache.openejb.core.stateful;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.core.Operations;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Index;

import javax.ejb.SessionBean;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * @org.apache.xbean.XBean element="statefulContainer"
 */
public class StatefulContainer implements org.apache.openejb.RpcContainer, TransactionContainer {

    private StatefulInstanceManager instanceManager;

    private HashMap<String,CoreDeploymentInfo> deploymentRegistry;

    private Object containerID = null;

    final static protected Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatefulContainer(Object id, TransactionManager transactionManager, SecurityService securityService, HashMap registry, Class passivator, int timeOut, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.deploymentRegistry = registry;
        this.containerID = id;
        this.transactionManager = transactionManager;
        this.securityService = securityService;

        instanceManager = new StatefulInstanceManager(transactionManager, securityService, passivator, timeOut, poolSize, bulkPassivate);

        for (CoreDeploymentInfo deploymentInfo : deploymentRegistry.values()) {
            Map<Method, MethodType> methods = getLifecycelMethodsOfInterface(deploymentInfo);

            deploymentInfo.setContainerData(new Data(new Index(methods)));
        }
    }

    private class Data {
        private final Index<Method,MethodType> methodIndex;

        private Data(Index<Method,MethodType> methodIndex) {
            this.methodIndex = methodIndex;
        }

        public Index<Method, MethodType> getMethodIndex() {
            return methodIndex;
        }
    }

    private Map<Method, MethodType> getLifecycelMethodsOfInterface(CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodType> methods = new HashMap();

        Method preDestroy = deploymentInfo.getPreDestroy();
        if (preDestroy != null){
            methods.put(preDestroy, MethodType.REMOVE);

            Class businessLocal = deploymentInfo.getBusinessLocalInterface();
            if (businessLocal != null){
                try {
                    Method method = businessLocal.getMethod(preDestroy.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {}
            }

            Class businessRemote = deploymentInfo.getBusinessRemoteInterface();
            if (businessRemote != null){
                try {
                    Method method = businessRemote.getMethod(preDestroy.getName());
                    methods.put(method, MethodType.REMOVE);
                } catch (NoSuchMethodException thatsFine) {}
            }
        }

        Class legacyRemote = deploymentInfo.getRemoteInterface();
        if (legacyRemote != null){
            try {
                Method method = legacyRemote.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {}
        }

        Class legacyLocal = deploymentInfo.getLocalInterface();
        if (legacyLocal != null){
            try {
                Method method = legacyLocal.getMethod("remove");
                methods.put(method, MethodType.REMOVE);
            } catch (NoSuchMethodException thatsFine) {}
        }

        Class homeInterface = deploymentInfo.getHomeInterface();
        if (homeInterface != null){
            for (Method method : homeInterface.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }

        Class localHomeInterface = deploymentInfo.getLocalHomeInterface();
        if (localHomeInterface != null){
            for (Method method : localHomeInterface.getMethods()) {
                if (method.getName().startsWith("create")){
                    methods.put(method, MethodType.CREATE);
                } else if (method.getName().equals("remove")){
                    methods.put(method, MethodType.REMOVE);
                }
            }
        }
        return methods;
    }

    private static enum MethodType {
        CREATE, REMOVE, BUSINESS;
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
        CoreDeploymentInfo di = (CoreDeploymentInfo) info;
        di.setContainer(this);
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws org.apache.openejb.OpenEJBException {
        try {

            CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);

            ThreadContext callContext = ThreadContext.getThreadContext();
            callContext.set(deployInfo, primKey, securityIdentity);

            boolean authorized = getSecurityService().isCallerAuthorized(securityIdentity, deployInfo.getAuthorizedRoles(callMethod));

            if (!authorized){
                throw new ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));
            }

            Data data = (Data) deployInfo.getContainerData();
            MethodType methodType = data.getMethodIndex().get(callMethod);
            methodType = (methodType != null) ? methodType : MethodType.BUSINESS;

            switch (methodType){
                case CREATE: return createEJBObject(callMethod, args, callContext);
                case REMOVE: removeEJBObject(callMethod, args, callContext); return null;
            }

            Object bean = instanceManager.obtainInstance(primKey, callContext);
            callContext.setCurrentOperation(Operations.OP_BUSINESS);
            Object returnValue = null;
            Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);

            returnValue = _invoke(callMethod, runMethod, args, bean, callContext);

            instanceManager.poolInstance(primKey, bean);

            return returnValue;

        } finally {
            /*
                The thread context must be stripped from the thread before returning or throwing an exception
                so that an object outside the container does not have access to a
                bean's JNDI ENC.  In addition, its important for the
                org.apache.openejb.core.ivm.java.javaURLContextFactory, which determines the context
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

    protected Object _invoke(Method callMethod, Method runMethod, Object [] args, Object bean, ThreadContext callContext)
            throws org.apache.openejb.OpenEJBException {

        TransactionPolicy txPolicy = callContext.getDeploymentInfo().getTransactionPolicy(callMethod);
        TransactionContext txContext = new TransactionContext(callContext, getTransactionManager());
        txContext.context.put(StatefulInstanceManager.class, instanceManager);
        try {
            txPolicy.beforeInvoke(bean, txContext);
        } catch (org.apache.openejb.ApplicationException e) {
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
            throws org.apache.openejb.OpenEJBException {

        try {
            Object bean = instanceManager.obtainInstance(callContext.getPrimaryKey(), callContext);
            if (bean != null) {
                callContext.setCurrentOperation(Operations.OP_REMOVE);
                Method preDestroy = callContext.getDeploymentInfo().getPreDestroy();
                if (preDestroy != null) {
                    _invoke(callMethod, preDestroy, null, bean, callContext);
                }
            }
        } finally {
            instanceManager.freeInstance(callContext.getPrimaryKey());
        }

    }

    protected ProxyInfo createEJBObject(Method callMethod, Object [] args, ThreadContext callContext)
            throws org.apache.openejb.OpenEJBException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) callContext.getDeploymentInfo();
        Class beanType = deploymentInfo.getBeanClass();
        Object primaryKey = this.newPrimaryKey();
        callContext.setPrimaryKey(primaryKey);

        Object bean = instanceManager.newInstance(primaryKey, beanType);

        // Do postConstructs or create(...)
        callContext.setCurrentOperation(Operations.OP_CREATE);
        if (bean instanceof SessionBean) {
            Method runMethod = deploymentInfo.getMatchingBeanMethod(callMethod);
            _invoke(callMethod, runMethod, args, bean, callContext);
        } else {
            Method postConstruct = deploymentInfo.getPostConstruct();
            if (postConstruct != null){
                _invoke(callMethod, postConstruct, args, bean, callContext);
            }
        }

        instanceManager.poolInstance(primaryKey, bean);

        Class callingClass = callMethod.getDeclaringClass();
        Class objectInterface = deploymentInfo.getObjectInterface(callingClass);
        return new ProxyInfo(deploymentInfo, primaryKey, objectInterface, this);
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
