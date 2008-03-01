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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.Injection;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.core.transaction.TransactionRolledbackException;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PojoSerialization;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.recipe.ConstructionException;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.io.ObjectStreamException;

public class StatefulInstanceManager {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    protected final long timeOut;

    // queue of beans for LRU algorithm
    private final BeanEntryQueue lruQueue;

    private final PassivationStrategy passivator;

    private final int bulkPassivationSize;

    private final TransactionManager transactionManager;
    private final SecurityService securityService;
    private final JtaEntityManagerRegistry jtaEntityManagerRegistry;

    public StatefulInstanceManager(TransactionManager transactionManager, SecurityService securityService, JtaEntityManagerRegistry jtaEntityManagerRegistry, Class passivatorClass, int timeout, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.jtaEntityManagerRegistry = jtaEntityManagerRegistry;
        this.lruQueue = new BeanEntryQueue(poolSize);
        if (poolSize == 0) {
            this.bulkPassivationSize = 1;
        } else {
            this.bulkPassivationSize = Math.min(bulkPassivate, poolSize);
        }
        this.timeOut = timeout * 60 * 1000;

        try {
            passivatorClass = (passivatorClass == null) ? SimplePassivater.class : passivatorClass;
            passivator = (PassivationStrategy) passivatorClass.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Could not create the passivator " + passivatorClass.getName(), e);
        }
    }

    public void deploy(CoreDeploymentInfo deploymentInfo, Index<Method, StatefulContainer.MethodType> index) throws OpenEJBException {
        deploymentInfo.setContainerData(new Data(index));
    }

    public void undeploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        Data data = (Data) deploymentInfo.getContainerData();
        if (data != null) {
            for (BeanEntry entry: data.getBeanIndex().values()) {
                lruQueue.remove(entry);
            }
            deploymentInfo.setContainerData(null);
        }
    }

    Index<Method, StatefulContainer.MethodType> getMethodIndex(CoreDeploymentInfo deploymentInfo) {
        Data data = (Data) deploymentInfo.getContainerData();
        return data.getMethodIndex();
    }

    public Transaction getBeanTransaction(ThreadContext callContext) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(callContext);
        if (entry == null) return null;
        return entry.beanTransaction;
    }

    public void setBeanTransaction(ThreadContext callContext, Transaction beanTransaction) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(callContext);
        entry.beanTransaction = beanTransaction;
    }

    public Map<EntityManagerFactory, EntityManager> getEntityManagers(ThreadContext callContext, Index<EntityManagerFactory, Map> factories) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(callContext);
        return entry.getEntityManagers(factories);
    }

    public void setEntityManagers(ThreadContext callContext, Index<EntityManagerFactory, EntityManager> entityManagers) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(callContext);
        entry.setEntityManagers(entityManagers);
    }

    public Object newInstance(Object primaryKey, Class beanClass) throws OpenEJBException {
        Object bean = null;

        ThreadContext threadContext = ThreadContext.getThreadContext();
        Operation currentOperation = threadContext.getCurrentOperation();
        try {
            ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
//            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

            ThreadContext callContext = ThreadContext.getThreadContext();
            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            Context ctx = deploymentInfo.getJndiEnc();
            SessionContext sessionContext;
            synchronized (this) {
                try {
                    sessionContext = (SessionContext) ctx.lookup("java:comp/EJBContext");
                } catch (NamingException e1) {
                    sessionContext = createSessionContext();
                    ctx.bind("java:comp/EJBContext", sessionContext);
                }
            }
            if (javax.ejb.SessionBean.class.isAssignableFrom(beanClass) || hasSetSessionContext(beanClass)) {
                callContext.setCurrentOperation(Operation.INJECTION);
                objectRecipe.setProperty("sessionContext", new StaticRecipe(sessionContext));
            }

            fillInjectionProperties(objectRecipe, beanClass, deploymentInfo, ctx);

            bean = objectRecipe.create(beanClass.getClassLoader());

            Map unsetProperties = objectRecipe.getUnsetProperties();
            if (unsetProperties.size() > 0) {
                for (Object property : unsetProperties.keySet()) {
                    logger.warning("Injection: No such property '" + property + "' in class " + beanClass.getName());
                }
            }
            HashMap<String, Object> interceptorInstances = new HashMap<String, Object>();
            for (InterceptorData interceptorData : deploymentInfo.getAllInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) continue;

                Class clazz = interceptorData.getInterceptorClass();
                ObjectRecipe interceptorRecipe = new ObjectRecipe(clazz);
                interceptorRecipe.allow(Option.FIELD_INJECTION);
                interceptorRecipe.allow(Option.PRIVATE_PROPERTIES);
                interceptorRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

                fillInjectionProperties(interceptorRecipe, clazz, deploymentInfo, ctx);

                try {
                    Object interceptorInstance = interceptorRecipe.create(clazz.getClassLoader());
                    interceptorInstances.put(clazz.getName(), interceptorInstance);
                } catch (ConstructionException e) {
                    throw new Exception("Failed to create interceptor: " + clazz.getName(), e);
                }
            }

            interceptorInstances.put(beanClass.getName(), bean);

            callContext.setCurrentOperation(Operation.POST_CONSTRUCT);

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(bean, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);
            interceptorStack.invoke();

            bean = newInstance(primaryKey, bean, interceptorInstances);

        } catch (Throwable callbackException) {
            /*
            In the event of an exception, OpenEJB is required to log the exception, evict the instance,
            and mark the transaction for rollback.  If there is a transaction to rollback, then the a
              javax.transaction.TransactionRolledbackException must be throw to the client. Otherwise a
            java.rmi.RemoteException is thrown to the client.
            See EJB 1.1 specification, section 12.3.2
            See EJB 2.0 specification, section 18.3.3
            */
            handleCallbackException(callbackException, bean, threadContext, "setSessionContext");
        } finally {
            threadContext.setCurrentOperation(currentOperation);
        }

        // add to index
        BeanEntry entry = newBeanEntry(primaryKey, bean);
        getBeanIndex(threadContext).put(primaryKey, entry);

        return bean;
    }

    protected Instance newInstance(Object primaryKey, Object bean, Map<String, Object> interceptorInstances) {
        return new Instance(bean, interceptorInstances);
    }

    protected BeanEntry newBeanEntry(Object primaryKey, Object bean) {
        return new BeanEntry(bean, primaryKey, timeOut);
    }

    private static void fillInjectionProperties(ObjectRecipe objectRecipe, Class clazz, CoreDeploymentInfo deploymentInfo, Context context) {
        for (Injection injection : deploymentInfo.getInjections()) {
            if (!injection.getTarget().isAssignableFrom(clazz)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object object = context.lookup("java:comp/env/" + jndiName);
                if (object instanceof String) {
                    String string = (String) object;
                    // Pass it in raw so it could be potentially converted to
                    // another data type by an xbean-reflect property editor
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), string);
                } else {
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), new StaticRecipe(object));
                }
            } catch (NamingException e) {
                logger.warning("Injection data not found in enc: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget() + "/" + injection.getName());
            }
        }
    }


    private boolean hasSetSessionContext(Class beanClass) {
        try {
            beanClass.getMethod("setSessionContext", SessionContext.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private SessionContext createSessionContext() {
        StatefulUserTransaction userTransaction = new StatefulUserTransaction(new CoreUserTransaction(transactionManager), jtaEntityManagerRegistry);
        return new StatefulContext(transactionManager, securityService, userTransaction);
    }

    public Object obtainInstance(Object primaryKey, ThreadContext callContext) throws OpenEJBException {
        if (primaryKey == null) {
            throw new SystemException(new NullPointerException("Cannot obtain an instance of the stateful session bean with a null session id"));
        }

        // look for entry in index
        BeanEntry entry = getBeanIndex(callContext).get(primaryKey);

        // if we didn't find the bean in the index, try to activate it
        if (entry == null) {
            Object bean = activateInstance(primaryKey, callContext);
            return bean;
        }

        // if the bean is already in a transaction, just return it
        if (entry.beanTransaction != null) {
            return entry.bean;
        }

        // remove from the queue so it is not passivated while in use
        BeanEntry queueEntry = lruQueue.remove(entry);
        if (queueEntry != null) {
            // if bean is timed out, destroy it
            if (entry.isTimedOut()) {
                entry = getBeanIndex(callContext).remove(entry.primaryKey);
                handleTimeout(entry, callContext);
                throw new InvalidateReferenceException(new NoSuchObjectException("Stateful SessionBean has timed-out"));
            }
            return entry.bean;
        } else {
            // if it is not in the queue, the bean is already being invoked
            // the only reentrant/concurrent operations allowed are Session synchronization callbacks
            Operation currentOperation = callContext.getCurrentOperation();
            if (currentOperation != Operation.AFTER_COMPLETION && currentOperation != Operation.BEFORE_COMPLETION) {
                throw new ApplicationException(new RemoteException("Concurrent calls not allowed"));
            }

            return entry.bean;
        }
    }

    private Object activateInstance(Object primaryKey, ThreadContext callContext) throws SystemException, ApplicationException {
        // attempt to active a passivated entity
        BeanEntry entry = activate(primaryKey);
        if (entry == null) {
            throw new InvalidateReferenceException(new NoSuchObjectException("Not Found"));
        }

        return activateInstance(callContext, entry);
    }

    public Object activateInstance(ThreadContext callContext, BeanEntry entry)
        throws SystemException, ApplicationException {
        if (entry.isTimedOut()) {
            // Since the bean instance hasn't had its ejbActivate() method called yet,
            // it is still considered to be passivated at this point. Instances that timeout
            // while passivated must be evicted WITHOUT having their ejbRemove()
            // method invoked. Section 6.6 of EJB 1.1 specification.
            throw new InvalidateReferenceException(new NoSuchObjectException("Timed Out"));
        }

        // call the activate method
        Operation currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operation.ACTIVATE);
        try {
            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

            StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) entry.bean;
            Method remove = instance.bean instanceof SessionBean? SessionBean.class.getMethod("ejbActivate"): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.ACTIVATE, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();
        } catch (Throwable callbackException) {
            /*
            In the event of an exception, OpenEJB is required to log the exception, evict the instance,
            and mark the transaction for rollback.  If there is a transaction to rollback, then the a
            javax.transaction.TransactionRolledbackException must be throw to the client. Otherwise a
            java.rmi.RemoteException is thrown to the client.
            See EJB 1.1 specification, section 12.3.2
            */
            handleCallbackException(callbackException, entry.bean, callContext, "ejbActivate");
        } finally {
            callContext.setCurrentOperation(currentOperation);
        }

        // add it to the index
        getBeanIndex(callContext).put(entry.primaryKey, entry);

        return entry.bean;
    }

    protected void handleTimeout(BeanEntry entry, ThreadContext threadContext) {
        Operation currentOperation = threadContext.getCurrentOperation();
        threadContext.setCurrentOperation(Operation.PRE_DESTROY);
        BaseContext.State[] originalAllowedStates = threadContext.setCurrentAllowedStates(StatefulContext.getStates());
        CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        Instance instance = (Instance) entry.bean;

        try {
            Method remove = instance.bean instanceof SessionBean? SessionBean.class.getMethod("ejbRemove"): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();
        } catch (Throwable callbackException) {
            /*
              Exceptions are processed "quietly"; they are not reported to the client since
              the timeout that caused the ejbRemove() operation did not, "technically", take
              place in the context of a client call. Logically, it may have timeout sometime
              before the client call.
            */
            String logMessage = "An unexpected exception occured while invoking the ejbRemove method on the timed-out Stateful SessionBean instance; " + callbackException.getClass().getName() + " " + callbackException.getMessage();

            /* [1] Log the exception or error */
            logger.error(logMessage);

        } finally {
            logger.info("Removing the timed-out stateful session bean instance " + entry.primaryKey);
            threadContext.setCurrentOperation(currentOperation);
            threadContext.setCurrentAllowedStates(originalAllowedStates);
        }
    }

    public void poolInstance(ThreadContext callContext, Object bean) throws OpenEJBException {
        // Don't pool if the bean has been undeployed
        if (callContext.getDeploymentInfo().isDestroyed()) return;

        Object primaryKey = callContext.getPrimaryKey();
        if (primaryKey == null || bean == null) {
            throw new SystemException("Invalid arguments");
        }

        BeanEntry entry = getBeanIndex(callContext).get(primaryKey);
        if (entry == null) {
            entry = activate(primaryKey);
            if (entry == null) {
                throw new SystemException("Invalid primaryKey:" + primaryKey);
            }
        } else if (entry.bean != bean) {
            throw new SystemException("Invalid ID for bean");
        }

        if (entry.beanTransaction == null) {
            if (callContext.getCurrentOperation() != Operation.CREATE){
                try {
                    entry.beanTransaction = transactionManager.getTransaction();
                } catch (javax.transaction.SystemException se) {
                    throw new SystemException("TransactionManager failure", se);
                }
            }

            // only put in LRU if no current transaction
            if (entry.beanTransaction == null) {
                // add it to end of Queue; the most reciently used bean
                lruQueue.add(entry);

                onPoolInstanceWithoutTransaction(callContext, entry);
            }
        }
    }

    protected void onPoolInstanceWithoutTransaction(ThreadContext callContext, BeanEntry entry) {
    }

    public Object freeInstance(ThreadContext threadContext) throws SystemException {
        Object primaryKey = threadContext.getPrimaryKey();
        BeanEntry entry = getBeanIndex(threadContext).remove(primaryKey);// remove frm index
        if (entry == null) {
            entry = activate(primaryKey);
        } else {
            lruQueue.remove(entry);
        }

        if (entry == null) {
            return null;
        }

        onFreeBeanEntry(threadContext, entry);

        return entry.bean;
    }

    protected void onFreeBeanEntry(ThreadContext threadContext, BeanEntry entry) {
    }

    protected void passivate() throws SystemException {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        Hashtable<Object, BeanEntry> stateTable = new Hashtable<Object, BeanEntry>(bulkPassivationSize);

        BeanEntry currentEntry;
        final Operation currentOperation = threadContext.getCurrentOperation();
        final BaseContext.State[] originalAllowedStates = threadContext.setCurrentAllowedStates(StatefulContext.getStates());
        CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        try {
            for (int i = 0; i < bulkPassivationSize; ++i) {
                currentEntry = lruQueue.first();
                if (currentEntry == null) {
                    break;
                }
                getBeanIndex(threadContext).remove(currentEntry.primaryKey);
                if (currentEntry.isTimedOut()) {
                    handleTimeout(currentEntry, threadContext);
                } else {
                    passivate(threadContext, currentEntry);
                    stateTable.put(currentEntry.primaryKey, currentEntry);
                }
            }
        } finally {
            threadContext.setCurrentOperation(currentOperation);
            threadContext.setCurrentAllowedStates(originalAllowedStates);
        }

        /*
           the IntraVmCopyMonitor.prePssivationOperation() demarcates 
           the begining of passivation; used by EjbHomeProxyHandler, 
           EjbObjectProxyHandler, IntraVmMetaData, and IntraVmHandle 
           to deterime how serialization for these artifacts.
        */
        try {
            IntraVmCopyMonitor.prePassivationOperation();

            passivator.passivate(stateTable);
        } finally {

            IntraVmCopyMonitor.postPassivationOperation();
        }
    }

    public void passivate(ThreadContext threadContext, BeanEntry currentEntry) {
        CoreDeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        threadContext.setCurrentOperation(Operation.PASSIVATE);
        try {
            StatefulInstanceManager.Instance instance = (StatefulInstanceManager.Instance) currentEntry.bean;

            Method passivate = instance.bean instanceof SessionBean? SessionBean.class.getMethod("ejbPassivate"): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, passivate, Operation.PASSIVATE, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();

        } catch (Throwable e) {

            String logMessage = "An unexpected exception occured while invoking the ejbPassivate method on the Stateful SessionBean instance; " + e.getClass().getName() + " " + e.getMessage();

            /* [1] Log the exception or error */
            logger.error(logMessage);
        }
    }

    protected BeanEntry activate(Object primaryKey) throws SystemException {
        return (BeanEntry) passivator.activate(primaryKey);
    }

    protected InvalidateReferenceException destroy(ThreadContext callContext, BeanEntry entry, Exception t) throws SystemException {

        getBeanIndex(callContext).remove(entry.primaryKey);// remove frm index
        lruQueue.remove(entry);// remove from queue
        if (entry.beanTransaction != null) {
            try {
                entry.beanTransaction.setRollbackOnly();
            } catch (javax.transaction.SystemException se) {
                throw new SystemException(se);
            } catch (IllegalStateException ise) {
                throw new SystemException("Attempt to rollback a non-tx context", ise);
            } catch (SecurityException lse) {
                throw new SystemException("Container not authorized to rollback tx", lse);
            }
            return new InvalidateReferenceException(new TransactionRolledbackException(t));
        } else if (t instanceof RemoteException) {
            return new InvalidateReferenceException(t);
        } else {
            EJBException e = (EJBException) t;
            return new InvalidateReferenceException(new RemoteException(e.getMessage(), e.getCausedByException()));
        }

    }

    protected BeanEntry getBeanEntry(ThreadContext callContext) throws OpenEJBException {
        Object primaryKey = callContext.getPrimaryKey();
        if (primaryKey == null) {
            throw new SystemException(new NullPointerException("The primary key is null. Cannot get the bean entry"));
        }
        BeanEntry entry = getBeanIndex(callContext).get(primaryKey);
        if (entry == null) {
            Object bean = this.obtainInstance(primaryKey, ThreadContext.getThreadContext());
            this.poolInstance(callContext, bean);
            entry = getBeanIndex(callContext).get(primaryKey);
        }
        return entry;
    }

    protected Hashtable<Object, BeanEntry> getBeanIndex(ThreadContext threadContext) {
        CoreDeploymentInfo deployment = threadContext.getDeploymentInfo();
        Data data = (Data) deployment.getContainerData();
        return data.getBeanIndex();
    }

    private static class Data {
        private final Index<Method, StatefulContainer.MethodType> methodIndex;
        private final Hashtable<Object, BeanEntry> beanIndex = new Hashtable<Object, BeanEntry>();

        private Data(Index<Method, StatefulContainer.MethodType> methodIndex) {
            this.methodIndex = methodIndex;
        }

        public Index<Method, StatefulContainer.MethodType> getMethodIndex() {
            return methodIndex;
        }

        public Hashtable<Object, BeanEntry> getBeanIndex() {
            return beanIndex;
        }
    }

    class BeanEntryQueue {
        private final LinkedList<BeanEntry> list;
        private final int capacity;

        protected BeanEntryQueue(int preferedCapacity) {
            capacity = preferedCapacity;
            list = new LinkedList<BeanEntry>();
        }

        protected synchronized BeanEntry first() {
            return list.removeFirst();
        }

        protected synchronized void add(BeanEntry entry) throws SystemException {
            entry.resetTimeOut();

            list.addLast(entry);
            entry.inQueue = true;

            if (list.size() >= capacity) {// is the LRU QUE full?
                passivate();
            }
        }

        protected synchronized BeanEntry remove(BeanEntry entry) {
            if (!entry.inQueue) {
                return null;
            }
            if (list.remove(entry)) {
                entry.inQueue = false;
                return entry;
            } else {

                return null;
            }
        }
    }


    protected void handleCallbackException(Throwable e, Object instance, ThreadContext callContext, String callBack) throws ApplicationException, SystemException {

        String remoteMessage = "An unexpected exception occured while invoking the " + callBack + " method on the Stateful SessionBean instance";
        String logMessage = remoteMessage + "; " + e.getClass().getName() + " " + e.getMessage();

        /* [1] Log the exception or error */
        logger.error(logMessage);

        /* [2] If the instance is in a transaction, mark the transaction for rollback. */
        Transaction transaction = null;
        try {
            transaction = transactionManager.getTransaction();
        } catch (Throwable t) {
            logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the " + callBack + " method of bean " + callContext.getPrimaryKey());
        }
        if (transaction != null) {
            markTxRollbackOnly(transaction);
        }

        /* [3] Discard the instance */
        freeInstance(callContext);

        /* [4] throw the java.rmi.RemoteException to the client */
        if (transaction == null) {
            throw new InvalidateReferenceException(new RemoteException(remoteMessage, e));
        } else {
            throw new InvalidateReferenceException(new TransactionRolledbackException(remoteMessage, e));
        }

    }

    protected void markTxRollbackOnly(Transaction tx) throws SystemException {
        try {
            if (tx != null) {
                tx.setRollbackOnly();
            }
        } catch (javax.transaction.SystemException se) {
            throw new SystemException(se);
        }
    }

    public static class Instance implements Serializable {
        public final Object bean;
        public final Map<String,Object> interceptors;

        public Instance(Object bean, Map<String, Object> interceptors) {
            this.bean = bean;
            this.interceptors = interceptors;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return new Serialization(this);
        }

        private static class Serialization implements Serializable {
            public final Object bean;
            public final Map<String,Object> interceptors;

            public Serialization(Instance i) {
                if (i.bean instanceof Serializable){
                    bean = i.bean;
                } else {
                    bean = new PojoSerialization(i.bean);
                }

                interceptors = new HashMap(i.interceptors.size());
                for (Map.Entry<String, Object> e : i.interceptors.entrySet()) {
                    if (e.getValue() == i.bean){
                        // need to use the same wrapped reference or well get two copies.
                        interceptors.put(e.getKey(), bean);
                    } else if (!(e.getValue() instanceof Serializable)){
                        interceptors.put(e.getKey(), new PojoSerialization(e.getValue()));
                    }
                }
            }

            protected Object readResolve() throws ObjectStreamException {
                // Anything wrapped with PojoSerialization will have been automatically
                // unwrapped via it's own readResolve so passing in the raw bean
                // and interceptors variables is totally fine.
                return new Instance(bean, interceptors);
            }
        }
    }
}

