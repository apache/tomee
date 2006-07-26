package org.openejb.core.stateful;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.core.CoreDeploymentInfo;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.IntraVmCopyMonitor;
import org.openejb.spi.SecurityService;
import org.openejb.util.Logger;
import org.openejb.util.SafeToolkit;

import javax.ejb.EJBException;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.rmi.RemoteException;
import java.util.Hashtable;

public class StatefulInstanceManager {

    protected long timeOUT = 0;

    protected Hashtable beanINDEX = new Hashtable();

    protected BeanEntryQue lruQUE;// que of beans for LRU algorithm

    protected PassivationStrategy passivator;

    protected int BULK_PASSIVATION_SIZE = 100;

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatefulInstanceManager(TransactionManager transactionManager, SecurityService securityService, Class passivatorClass, int timeout, int poolSize, int bulkPassivate) throws OpenEJBException {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.lruQUE = new BeanEntryQue(poolSize);
        this.BULK_PASSIVATION_SIZE = (bulkPassivate > poolSize) ? poolSize : bulkPassivate;
        this.timeOUT = timeout * 60 * 1000;

        try {
            passivatorClass = (passivatorClass == null) ? SimplePassivater.class : passivatorClass;
            passivator = (PassivationStrategy) passivatorClass.newInstance();
        } catch (Exception e) {
            throw new OpenEJBException("Could not create the passivator " + passivatorClass.getName(), e);
        }

    }

    public Object getAncillaryState(Object primaryKey)
            throws OpenEJBException {
        return this.getBeanEntry(primaryKey).ancillaryState;
    }

    public void setAncillaryState(Object primaryKey, Object ancillaryState)
            throws OpenEJBException {
        BeanEntry entry = getBeanEntry(primaryKey);
        entry.ancillaryState = ancillaryState;
        if (ancillaryState instanceof javax.transaction.Transaction)
            entry.transaction = (javax.transaction.Transaction) ancillaryState;

    }

    public EnterpriseBean newInstance(Object primaryKey, Class beanClass)
            throws OpenEJBException {
        return this.newInstance(primaryKey, null, beanClass);
    }

    public EnterpriseBean newInstance(Object primaryKey, Object ancillaryState, Class beanClass)
            throws OpenEJBException {

        SessionBean bean = null;

        try {
            bean = (SessionBean) toolkit.newInstance(beanClass);
        } catch (OpenEJBException oee) {
            logger.error("Can't instantiate new instance of class +" + beanClass.getName() + ". Received exception " + oee, oee);
            throw (SystemException) oee;
        }

        ThreadContext thrdCntx = ThreadContext.getThreadContext();
        byte currentOp = thrdCntx.getCurrentOperation();
        thrdCntx.setCurrentOperation(Operations.OP_SET_CONTEXT);
        try {
            CoreDeploymentInfo deploymentInfo = thrdCntx.getDeploymentInfo();
            bean.setSessionContext(createSessionContext());
        } catch (Throwable callbackException) {
            /*
            In the event of an exception, OpenEJB is required to log the exception, evict the instance,
            and mark the transaction for rollback.  If there is a transaction to rollback, then the a
            javax.transaction.TransactionRolledbackException must be throw to the client. Otherwise a
            java.rmi.RemoteException is thrown to the client.
            See EJB 1.1 specification, section 12.3.2
            See EJB 2.0 specification, section 18.3.3
            */
            handleCallbackException(callbackException, bean, thrdCntx, "setSessionContext");
        } finally {
            thrdCntx.setCurrentOperation(currentOp);
        }

        BeanEntry entry = new BeanEntry(bean, primaryKey, ancillaryState, timeOUT);

        beanINDEX.put(primaryKey, entry);

        return entry.bean;
    }

    private SessionContext createSessionContext() {
        return (SessionContext) new StatefulContext(transactionManager, securityService);
    }

    public SessionBean obtainInstance(Object primaryKey, ThreadContext callContext) throws OpenEJBException {
        if (primaryKey == null) {
            throw new org.openejb.SystemException(new NullPointerException("Cannot obtain an instance of the stateful session bean with a null session id"));
        }

        BeanEntry entry = (BeanEntry) beanINDEX.get(primaryKey);
        if (entry == null) {

            entry = activate(primaryKey);
            if (entry != null) {

                if (entry.isTimedOut()) {
                    /* Since the bean instance hasn't had its ejbActivate() method called yet, 
                       it is still considered to be passivated at this point. Instances that timeout 
                       while passivated must be evicted WITHOUT having their ejbRemove() 
                       method invoked. Section 6.6 of EJB 1.1 specification.  
                    */
                    throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Timed Out"));
                }

                byte currentOp = callContext.getCurrentOperation();
                callContext.setCurrentOperation(Operations.OP_ACTIVATE);

                try {
                    entry.bean.ejbActivate();
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
                    callContext.setCurrentOperation(currentOp);
                }

                beanINDEX.put(primaryKey, entry);
                return entry.bean;
            } else {
                throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Not Found"));
            }
        } else {// bean has been created and is pooled
            if (entry.transaction != null) {

                try {
                    if (entry.transaction.getStatus() == Status.STATUS_ACTIVE) {

//                        if(entry.transaction.equals(OpenEJB.getTransactionManager().getTransaction()))
                        return entry.bean;
//                        else
//                            throw new ApplicationException(new javax.transaction.InvalidTransactionException());
                    } else {

                        entry.transaction = null;
                        return entry.bean;
                    }
                } catch (javax.transaction.SystemException se) {
                    throw new org.openejb.SystemException(se);
                } catch (IllegalStateException ise) {
                    throw new org.openejb.SystemException(ise);
                } catch (java.lang.SecurityException lse) {
                    throw new org.openejb.SystemException(lse);
                }
            } else {// bean is pooled in the "method ready" pool.

                BeanEntry queEntry = lruQUE.remove(entry);// remove from Que so its not passivated while in use
                if (queEntry != null) {
                    if (entry.isTimedOut()) {

                        entry = (BeanEntry) beanINDEX.remove(entry.primaryKey);// remove frm index
                        handleTimeout(entry, callContext);
                        throw new org.openejb.InvalidateReferenceException(new java.rmi.NoSuchObjectException("Stateful SessionBean has timed-out"));
                    }
                    return entry.bean;
                } else {
                    byte currentOperation = callContext.getCurrentOperation();
                    if (currentOperation == Operations.OP_AFTER_COMPLETION || currentOperation == Operations.OP_BEFORE_COMPLETION)
                    {
                        return entry.bean;
                    } else {

                        throw new ApplicationException(new RemoteException("Concurrent calls not allowed"));
                    }
                }
            }
        }
    }

    protected void handleTimeout(BeanEntry entry, ThreadContext thrdCntx) {

        byte currentOp = thrdCntx.getCurrentOperation();
        thrdCntx.setCurrentOperation(Operations.OP_REMOVE);

        try {
            entry.bean.ejbRemove();
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
            thrdCntx.setCurrentOperation(currentOp);
        }
    }

    public void poolInstance(Object primaryKey, EnterpriseBean bean)
            throws OpenEJBException {
        if (primaryKey == null || bean == null)
            throw new SystemException("Invalid arguments");

        BeanEntry entry = (BeanEntry) beanINDEX.get(primaryKey);
        if (entry == null) {
            entry = activate(primaryKey);
            if (entry == null) {
                throw new SystemException("Invalid primaryKey:" + primaryKey);
            }
        } else if (entry.bean != bean)
            throw new SystemException("Invalid ID for bean");

        if (entry.transaction != null && entry.transaction == entry.ancillaryState) {

            return;// don't put in LRU (method ready) pool.
        } else {
            try {
                entry.transaction = getTransactionManager().getTransaction();
            } catch (javax.transaction.SystemException se) {
                throw new org.openejb.SystemException("TransactionManager failure");
            }

            if (entry.transaction == null) {// only put in LRU if no current transaction
                lruQUE.add(entry);// add it to end of Que; the most reciently used bean
            }
        }
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public EnterpriseBean freeInstance(Object primaryKey)
            throws org.openejb.SystemException {
        BeanEntry entry = null;
        entry = (BeanEntry) beanINDEX.remove(primaryKey);// remove frm index
        if (entry == null) {
            entry = activate(primaryKey);
        } else {
            lruQUE.remove(entry);
        }

        if (entry == null)
            return null;

        return entry.bean;
    }

    protected void passivate() throws SystemException {
        final ThreadContext thrdCntx = ThreadContext.getThreadContext();
        Hashtable stateTable = new Hashtable(BULK_PASSIVATION_SIZE);

        BeanEntry currentEntry;
        final byte currentOp = thrdCntx.getCurrentOperation();
        try {
            for (int i = 0; i < BULK_PASSIVATION_SIZE; ++i) {
                currentEntry = lruQUE.first();
                if (currentEntry == null)
                    break;
                beanINDEX.remove(currentEntry.primaryKey);
                if (currentEntry.isTimedOut()) {
                    handleTimeout(currentEntry, thrdCntx);
                } else {
                    thrdCntx.setCurrentOperation(Operations.OP_PASSIVATE);
                    try {

                        currentEntry.bean.ejbPassivate();
                    } catch (Throwable e) {

                        String logMessage = "An unexpected exception occured while invoking the ejbPassivate method on the Stateful SessionBean instance; " + e.getClass().getName() + " " + e.getMessage();

                        /* [1] Log the exception or error */
                        logger.error(logMessage);
                    }
                    stateTable.put(currentEntry.primaryKey, currentEntry);
                }
            }
        } finally {
            thrdCntx.setCurrentOperation(currentOp);
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

    protected org.openejb.InvalidateReferenceException destroy(BeanEntry entry, Exception t)
            throws org.openejb.SystemException {

        beanINDEX.remove(entry.primaryKey);// remove frm index
        lruQUE.remove(entry);// remove from que
        if (entry.transaction != null) {
            try {
                entry.transaction.setRollbackOnly();
            } catch (javax.transaction.SystemException se) {
                throw new org.openejb.SystemException(se);
            } catch (IllegalStateException ise) {
                throw new org.openejb.SystemException("Attempt to rollback a non-tx context", ise);
            } catch (java.lang.SecurityException lse) {
                throw new org.openejb.SystemException("Container not authorized to rollback tx", lse);
            }
            return new org.openejb.InvalidateReferenceException(new javax.transaction.TransactionRolledbackException(t.getMessage()));
        } else if (t instanceof RemoteException)
            return new org.openejb.InvalidateReferenceException(t);
        else {
            EJBException ejbE = (EJBException) t;
            return new org.openejb.InvalidateReferenceException(new RemoteException(ejbE.getMessage(), ejbE.getCausedByException()));
        }

    }

    protected BeanEntry getBeanEntry(Object primaryKey)
            throws OpenEJBException {
        if (primaryKey == null) {
            throw new SystemException(new NullPointerException("The primary key is null. Cannot get the bean entry"));
        }
        BeanEntry entry = (BeanEntry) beanINDEX.get(primaryKey);
        if (entry == null) {
            EnterpriseBean bean = this.obtainInstance(primaryKey, ThreadContext.getThreadContext());
            this.poolInstance(primaryKey, bean);
            entry = (BeanEntry) beanINDEX.get(primaryKey);
        }
        return entry;
    }

    class BeanEntryQue {
        private final java.util.LinkedList list;
        private final int capacity;

        protected BeanEntryQue(int preferedCapacity) {
            capacity = preferedCapacity;
            list = new java.util.LinkedList();
        }

        protected synchronized BeanEntry first() {
            return (BeanEntry) list.removeFirst();
        }

        protected synchronized void add(BeanEntry entry) throws org.openejb.SystemException {
            if (list.size() >= capacity) {// is the LRU QUE full?
                passivate();
            }
            entry.resetTimeOut();

            list.addLast(entry);
            entry.inQue = true;
        }

        protected synchronized BeanEntry remove(BeanEntry entry) {
            if (!entry.inQue)
                return null;
            if (list.remove(entry) == true) {
                entry.inQue = false;
                return entry;
            } else {

                return null;
            }
        }
    }

    public Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    protected void handleCallbackException(Throwable e, EnterpriseBean instance, ThreadContext callContext, String callBack) throws ApplicationException, org.openejb.SystemException {

        String remoteMessage = "An unexpected exception occured while invoking the " + callBack + " method on the Stateful SessionBean instance";
        String logMessage = remoteMessage + "; " + e.getClass().getName() + " " + e.getMessage();

        /* [1] Log the exception or error */
        logger.error(logMessage);

        /* [2] If the instance is in a transaction, mark the transaction for rollback. */
        Transaction tx = null;
        try {
            tx = getTransactionManager().getTransaction();
        } catch (Throwable t) {
            logger.error("Could not retreive the current transaction from the transaction manager while handling a callback exception from the " + callBack + " method of bean " + callContext.getPrimaryKey());
        }
        if (tx != null) markTxRollbackOnly(tx);

        /* [3] Discard the instance */
        freeInstance(callContext.getPrimaryKey());

        /* [4] throw the java.rmi.RemoteException to the client */
        if (tx == null) {
            throw new InvalidateReferenceException(new RemoteException(remoteMessage, e));
        } else {
            throw new InvalidateReferenceException(new javax.transaction.TransactionRolledbackException(logMessage));
        }

    }

    protected void markTxRollbackOnly(Transaction tx) throws SystemException {
        try {
            if (tx != null) tx.setRollbackOnly();
        } catch (javax.transaction.SystemException se) {
            throw new org.openejb.SystemException(se);
        }
    }
}

