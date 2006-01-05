package org.openejb.core.stateless;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.TransactionManager;

import org.apache.log4j.Category;
import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.OpenEJB;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.util.LinkedListStack;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.Stack;

public class StatelessInstanceManager {

    protected java.util.HashMap poolMap = new HashMap();
    protected int poolLimit = 0;
    protected int beanCount = 0;
    protected boolean strictPooling = false;

    protected PoolQueue poolQueue = null;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    protected final static Category logger = Category.getInstance("OpenEJB");
    private TransactionManager transactionManager;

    public StatelessInstanceManager() {
    }

    public void init(Properties props) throws OpenEJBException {
        transactionManager = (TransactionManager) props.get("TransactionManager");

        SafeProperties safeProps = toolkit.getSafeProperties(props);

        poolLimit = safeProps.getPropertyAsInt(EnvProps.IM_POOL_SIZE, 10);
        strictPooling = safeProps.getPropertyAsBoolean(EnvProps.IM_STRICT_POOLING, new Boolean(false)).booleanValue();
        if (strictPooling) {
            int waitTime = safeProps.getPropertyAsInt(EnvProps.IM_TIME_OUT, 0);
            poolQueue = new PoolQueue(waitTime);
        }
    }

    public EnterpriseBean getInstance(ThreadContext callContext)
            throws OpenEJBException {
        SessionBean bean = null;
        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
        Stack pool = (Stack) poolMap.get(deploymentId);
        if (pool == null) {
            pool = new LinkedListStack(poolLimit);
            poolMap.put(deploymentId, pool);
        } else
            bean = (SessionBean) pool.pop();

        while (strictPooling && bean == null && pool.size() >= poolLimit) {
            poolQueue.waitForAvailableInstance();
            bean = (SessionBean) pool.pop();
        }

        if (bean == null) {
            try {
                Class beanClass = callContext.getDeploymentInfo().getBeanClass();
                bean = (SessionBean) toolkit.newInstance(beanClass);
            } catch (OpenEJBException oee) {
                throw (SystemException) oee;
            }

            byte originalOperation = callContext.getCurrentOperation();
            try {

                callContext.setCurrentOperation(Operations.OP_SET_CONTEXT);
                DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
                bean.setSessionContext(createSessionContext());

                callContext.setCurrentOperation(Operations.OP_CREATE);
                Method createMethod = deploymentInfo.getCreateMethod();
                createMethod.invoke(bean, null);
            } catch (Throwable e) {
                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
                }
                String t = "The bean instance " + bean + " threw a system exception:" + e;
                logger.error(t, e);
                throw new org.openejb.ApplicationException(new RemoteException("Can not obtain a free instance."));
            } finally {
                callContext.setCurrentOperation(originalOperation);
            }
        }
        return bean;
    }

    private SessionContext createSessionContext() {
        return (SessionContext) new StatelessContext(transactionManager, OpenEJB.getSecurityService());
    }

    public void poolInstance(ThreadContext callContext, EnterpriseBean bean)
            throws OpenEJBException {
        if (bean == null)
            throw new SystemException("Invalid arguments");
        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
        Stack pool = (Stack) poolMap.get(deploymentId);
        if (strictPooling) {
            pool.push(bean);
            poolQueue.notifyWaitingThreads();
        } else {
            if (pool.size() > poolLimit)
                freeInstance(callContext, bean);
            else
                pool.push(bean);
        }

    }

    public void freeInstance(ThreadContext callContext, EnterpriseBean bean) {
        try {
            callContext.setCurrentOperation(Operations.OP_REMOVE);
            ((SessionBean) bean).ejbRemove();
        } catch (Throwable re) {

            logger.error("The bean instance " + bean + " threw a system exception:" + re, re);
        }

    }

    public void discardInstance(ThreadContext callContext, EnterpriseBean bean) {

    }

    static class PoolQueue {
        private final long waitPeriod;

        public PoolQueue(long time) {
            waitPeriod = time;
        }

        public synchronized void waitForAvailableInstance()
                throws org.openejb.InvalidateReferenceException {
            try {
                wait(waitPeriod);
            } catch (InterruptedException ie) {
                throw new org.openejb.InvalidateReferenceException(new RemoteException("No instance avaiable to service request"));
            }
        }

        public synchronized void notifyWaitingThreads() {
            notify();
        }
    }

}
