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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.Injection;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;

public class StatefulInstanceManager {
    public static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final long timeOut;

    private final Hashtable<Object, BeanEntry> beanIndex = new Hashtable<Object, BeanEntry>();

    // queue of beans for LRU algorithm
    private final BeanEntryQueue lruQueue;

    private final PassivationStrategy passivator;

    private final int bulkPassivationSize;

    private final TransactionManager transactionManager;
    private final SecurityService securityService;

    public StatefulInstanceManager(TransactionManager transactionManager, SecurityService securityService, Class passivatorClass, int timeout, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
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

    public Transaction getBeanTransaction(Object primaryKey) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(primaryKey);
        return entry.beanTransaction;
    }

    public void setBeanTransaction(Object primaryKey, Transaction beanTransaction) throws OpenEJBException {
        BeanEntry entry = getBeanEntry(primaryKey);
        entry.beanTransaction = beanTransaction;
    }

    public Object newInstance(Object primaryKey, Class beanClass) throws OpenEJBException {
        Object bean = null;

        ThreadContext threadContext = ThreadContext.getThreadContext();
        Operation currentOperation = threadContext.getCurrentOperation();
        threadContext.setCurrentOperation(Operation.OP_SET_CONTEXT);
        try {
            ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

            ThreadContext callContext = ThreadContext.getThreadContext();
            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            Context ctx = deploymentInfo.getJndiEnc();
            for (Injection injection : deploymentInfo.getInjections()) {
                try {
                    String jndiName = injection.getJndiName();
                    Object object = ctx.lookup("java:comp/env/" + jndiName);
                    objectRecipe.setProperty(injection.getName(), new StaticRecipe(object));
                } catch (NamingException e) {
                    logger.warning("Injection data not found in enc: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget() + "/" + injection.getName());
                }
            }

            objectRecipe.setProperty("sessionContext", new StaticRecipe(createSessionContext()));

            bean = objectRecipe.create(beanClass.getClassLoader());
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
        BeanEntry entry = new BeanEntry(bean, primaryKey, timeOut);
        beanIndex.put(primaryKey, entry);

        return bean;
    }

    private SessionContext createSessionContext() {
        return new StatefulContext(transactionManager, securityService);
    }

    public Object obtainInstance(Object primaryKey, ThreadContext callContext) throws OpenEJBException {
        if (primaryKey == null) {
            throw new SystemException(new NullPointerException("Cannot obtain an instance of the stateful session bean with a null session id"));
        }

        // look for entry in index
        BeanEntry entry = beanIndex.get(primaryKey);

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
                entry = beanIndex.remove(entry.primaryKey);
                handleTimeout(entry, callContext);
                throw new InvalidateReferenceException(new NoSuchObjectException("Stateful SessionBean has timed-out"));
            }
            return entry.bean;
        } else {
            // if it is not in the queue, the bean is already being invoked
            // the only reentrant/concurrent operations allowed are Session synchronization callbacks
            Operation currentOperation = callContext.getCurrentOperation();
            if (currentOperation != Operation.OP_AFTER_COMPLETION && currentOperation != Operation.OP_BEFORE_COMPLETION) {
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

        if (entry.isTimedOut()) {
            // Since the bean instance hasn't had its ejbActivate() method called yet,
            // it is still considered to be passivated at this point. Instances that timeout
            // while passivated must be evicted WITHOUT having their ejbRemove()
            // method invoked. Section 6.6 of EJB 1.1 specification.
            throw new InvalidateReferenceException(new NoSuchObjectException("Timed Out"));
        }

        // call the activate method
        Operation currentOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(Operation.OP_ACTIVATE);
        try {
            Method postActivate = callContext.getDeploymentInfo().getPostActivate();
            if (postActivate != null) {
                try {
                    postActivate.invoke(entry.bean);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
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
        beanIndex.put(primaryKey, entry);

        return entry.bean;
    }

    protected void handleTimeout(BeanEntry entry, ThreadContext threadContext) {
        Operation currentOperation = threadContext.getCurrentOperation();
        threadContext.setCurrentOperation(Operation.OP_REMOVE);

        try {
            Method preDestroy = threadContext.getDeploymentInfo().getPreDestroy();
            if (preDestroy != null) {
                try {
                    preDestroy.invoke(entry.bean);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
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
        }
    }

    public void poolInstance(Object primaryKey, Object bean) throws OpenEJBException {
        if (primaryKey == null || bean == null) {
            throw new SystemException("Invalid arguments");
        }

        BeanEntry entry = beanIndex.get(primaryKey);
        if (entry == null) {
            entry = activate(primaryKey);
            if (entry == null) {
                throw new SystemException("Invalid primaryKey:" + primaryKey);
            }
        } else if (entry.bean != bean) {
            throw new SystemException("Invalid ID for bean");
        }

        if (entry.beanTransaction == null) {
            try {
                entry.beanTransaction = transactionManager.getTransaction();
            } catch (javax.transaction.SystemException se) {
                throw new SystemException("TransactionManager failure");
            }

            // only put in LRU if no current transaction
            if (entry.beanTransaction == null) {
                // add it to end of Queue; the most reciently used bean
                lruQueue.add(entry);
            }
        }
    }

    public Object freeInstance(Object primaryKey) throws SystemException {
        BeanEntry entry = null;
        entry = beanIndex.remove(primaryKey);// remove frm index
        if (entry == null) {
            entry = activate(primaryKey);
        } else {
            lruQueue.remove(entry);
        }

        if (entry == null) {
            return null;
        }

        return entry.bean;
    }

    protected void passivate() throws SystemException {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        Hashtable<Object, BeanEntry> stateTable = new Hashtable<Object, BeanEntry>(bulkPassivationSize);

        BeanEntry currentEntry;
        final Operation currentOperation = threadContext.getCurrentOperation();
        Method prePassivate = threadContext.getDeploymentInfo().getPrePassivate();
        try {
            for (int i = 0; i < bulkPassivationSize; ++i) {
                currentEntry = lruQueue.first();
                if (currentEntry == null) {
                    break;
                }
                beanIndex.remove(currentEntry.primaryKey);
                if (currentEntry.isTimedOut()) {
                    handleTimeout(currentEntry, threadContext);
                } else {
                    threadContext.setCurrentOperation(Operation.OP_PASSIVATE);
                    try {
                        if (prePassivate != null) {
                            // TODO Are all beans in the stateTable the same type?
                            try {
                                prePassivate.invoke(currentEntry.bean);
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        }
                    } catch (Throwable e) {

                        String logMessage = "An unexpected exception occured while invoking the ejbPassivate method on the Stateful SessionBean instance; " + e.getClass().getName() + " " + e.getMessage();

                        /* [1] Log the exception or error */
                        logger.error(logMessage);
                    }
                    stateTable.put(currentEntry.primaryKey, currentEntry);
                }
            }
        } finally {
            threadContext.setCurrentOperation(currentOperation);
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

    protected BeanEntry activate(Object primaryKey) throws SystemException {
        return (BeanEntry) passivator.activate(primaryKey);
    }

    protected InvalidateReferenceException destroy(BeanEntry entry, Exception t) throws SystemException {

        beanIndex.remove(entry.primaryKey);// remove frm index
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
            return new InvalidateReferenceException(new TransactionRolledbackException(t.getMessage()));
        } else if (t instanceof RemoteException) {
            return new InvalidateReferenceException(t);
        } else {
            EJBException e = (EJBException) t;
            return new InvalidateReferenceException(new RemoteException(e.getMessage(), e.getCausedByException()));
        }

    }

    protected BeanEntry getBeanEntry(Object primaryKey) throws OpenEJBException {
        if (primaryKey == null) {
            throw new SystemException(new NullPointerException("The primary key is null. Cannot get the bean entry"));
        }
        BeanEntry entry = beanIndex.get(primaryKey);
        if (entry == null) {
            Object bean = this.obtainInstance(primaryKey, ThreadContext.getThreadContext());
            this.poolInstance(primaryKey, bean);
            entry = beanIndex.get(primaryKey);
        }
        return entry;
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
        freeInstance(callContext.getPrimaryKey());

        /* [4] throw the java.rmi.RemoteException to the client */
        if (transaction == null) {
            throw new InvalidateReferenceException(new RemoteException(remoteMessage, e));
        } else {
            throw new InvalidateReferenceException(new TransactionRolledbackException(logMessage));
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
}

