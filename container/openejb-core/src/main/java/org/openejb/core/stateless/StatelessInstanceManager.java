package org.openejb.core.stateless;

import org.apache.log4j.Category;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;
import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.spi.SecurityService;
import org.openejb.util.LinkedListStack;
import org.openejb.util.SafeToolkit;
import org.openejb.util.Stack;

import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;

public class StatelessInstanceManager {

    protected java.util.HashMap poolMap = new HashMap();
    protected int poolLimit = 0;
    protected int beanCount = 0;
    protected boolean strictPooling = false;

    protected PoolQueue poolQueue = null;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    protected final static Category logger = Category.getInstance("OpenEJB");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatelessInstanceManager(TransactionManager transactionManager, SecurityService securityService, int timeout, int poolSize, boolean strictPooling) {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.poolLimit = poolSize;
        this.strictPooling = strictPooling;

        if (this.strictPooling) {
            poolQueue = new PoolQueue(timeout);
        }
    }

    public Object getInstance(ThreadContext callContext)
            throws OpenEJBException {
        Object bean = null;
        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
        Stack pool = (Stack) poolMap.get(deploymentId);
        if (pool == null) {
            pool = new LinkedListStack(poolLimit);
            poolMap.put(deploymentId, pool);
        } else
            bean = pool.pop();

        while (strictPooling && bean == null && pool.size() >= poolLimit) {
            poolQueue.waitForAvailableInstance();
            bean = pool.pop();
        }

        if (bean == null) {

            Class beanClass = callContext.getDeploymentInfo().getBeanClass();
            ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);

            byte originalOperation = callContext.getCurrentOperation();
            try {

                callContext.setCurrentOperation(Operations.OP_SET_CONTEXT);
                objectRecipe.setProperty("sessionContext", new StaticRecipe(createSessionContext()));
                bean = objectRecipe.create();
                callContext.setCurrentOperation(Operations.OP_CREATE);

                DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
                Method postConstruct = deploymentInfo.getPostConstruct();
                if (postConstruct != null){
                    postConstruct.invoke(bean);
                }
            } catch (Throwable e) {
                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
                }
                String t = "The bean instance " + bean + " threw a system exception:" + e;
                logger.error(t, e);
                throw new org.openejb.ApplicationException(new RemoteException("Can not obtain a free instance.",e));
            } finally {
                callContext.setCurrentOperation(originalOperation);
            }
        }
        return bean;
    }

    private SessionContext createSessionContext() {
        return (SessionContext) new StatelessContext(transactionManager, securityService);
    }

    public void poolInstance(ThreadContext callContext, Object bean)
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

    public void freeInstance(ThreadContext callContext, Object bean) {
        try {
            callContext.setCurrentOperation(Operations.OP_REMOVE);
            Method preDestroy = callContext.getDeploymentInfo().getPreDestroy();
            if (preDestroy != null){
                preDestroy.invoke(bean);
            }
        } catch (Throwable re) {

            logger.error("The bean instance " + bean + " threw a system exception:" + re, re);
        }

    }

    public void discardInstance(ThreadContext callContext, Object bean) {

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
