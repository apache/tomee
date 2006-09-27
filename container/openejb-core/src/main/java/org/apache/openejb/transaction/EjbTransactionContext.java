/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.EJBInstanceContext;

/**
 * @version $Rev$ $Date$
 */
public class EjbTransactionContext {
    private static final Log log = LogFactory.getLog(EjbTransactionContext.class);

    private static final ConcurrentHashMap DATA_INDEX = new ConcurrentHashMap();

    public static EjbTransactionContext get(Transaction transaction) {
        if (transaction == null) {
            return new EjbTransactionContext(null);
        }

        EjbTransactionContext ctx = (EjbTransactionContext) DATA_INDEX.get(transaction);
        if (ctx == null) {
            ctx = new EjbTransactionContext(transaction);
            try {
                transaction.registerSynchronization(new EjbSynchronization(ctx));
            } catch (RollbackException e) {
                throw (IllegalStateException) new IllegalStateException("Transaction is already rolled back").initCause(e);
            } catch (SystemException e) {
                throw new RuntimeException("Unable to register ejb transaction synchronization callback", e);
            }

            // Note: no synchronization is necessary here.  Since a transaction can only be associated with a single
            // thread at a time, it should not be possible for someone else to have snuck in and created a
            // ConnectorTransactionContext for this transaction.  We still protect against that with the putIfAbsent
            // call below, and we simply have an extra transaction synchronization registered that won't do anything
            DATA_INDEX.putIfAbsent(transaction, ctx);
        }
        return ctx;
    }

    private static void remove(Transaction transaction) {
        DATA_INDEX.remove(transaction);
    }


    private final Transaction transaction;

    // context tracking
    private EJBInstanceContext currentContext;
    private final DoubleKeyedHashMap associatedContexts = new DoubleKeyedHashMap();
    private final DoubleKeyedHashMap dirtyContexts = new DoubleKeyedHashMap();

    // cmp data
    private CmpTxData cmpTxData;

    public EjbTransactionContext(Transaction transaction) {
        this.transaction = transaction;
    }

    public void associate(EJBInstanceContext context) throws Throwable {
        if (associatedContexts.put(context.getContainerId(), context.getId(), context) == null) {
            context.associate();
        }
    }

    public void unassociate(EJBInstanceContext context) throws Throwable {
        associatedContexts.remove(context.getContainerId(), context.getId());
        context.unassociate();
    }

    public void unassociate(Object containerId, Object id) throws Throwable {
        EJBInstanceContext context = (EJBInstanceContext) associatedContexts.remove(containerId, id);
        if (context != null) {
            context.unassociate();
        }
    }

    public EJBInstanceContext getContext(Object containerId, Object id) {
        return (EJBInstanceContext) associatedContexts.get(containerId, id);
    }

    public EJBInstanceContext beginInvocation(EJBInstanceContext newContext) throws Throwable {
        if (newContext.getId() != null) {
            associate(newContext);
            dirtyContexts.put(newContext.getContainerId(), newContext.getId(), newContext);
        }
        newContext.enter();
        EJBInstanceContext caller = currentContext;
        currentContext = newContext;
        return caller;
    }

    public void endInvocation(EJBInstanceContext oldContext) {
        EJBInstanceContext x = currentContext;
        if (x != null) {
            x.exit();
        }
        currentContext = oldContext;
    }

    public CmpTxData getCmpTxData() {
        return cmpTxData;
    }

    public void setCmpTxData(CmpTxData cmpTxData) {
        this.cmpTxData = cmpTxData;
    }

    public void complete(boolean succeeded) {
        if (transaction != null) {
            // jta based transactions are completed via the Synchronization callback below
            return;
        }
        try {
            if (succeeded) {
                flush();
            }
        } catch (Error e) {
            throw e;
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable throwable) {
            log.error("Exception occured during complete", throwable);
        } finally {
            unassociateAll();
        }
    }

    public void flush() throws Throwable {
        while (!dirtyContexts.isEmpty()) {
            ArrayList toFlush = new ArrayList(dirtyContexts.values());
            dirtyContexts.clear();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                EJBInstanceContext context = (EJBInstanceContext) i.next();
                if (!context.isDead()) {
                    context.flush();
                }
            }
        }
        if (currentContext != null && currentContext.getId() != null) {
            dirtyContexts.put(currentContext.getContainerId(), currentContext.getId(), currentContext);
        }
        if(cmpTxData != null) {
            cmpTxData.flush();
        }
    }

    private void beforeCompletion() {
        try {
            while (!dirtyContexts.isEmpty()) {
                ArrayList toFlush = new ArrayList(dirtyContexts.values());
                dirtyContexts.clear();
                for (Iterator i = toFlush.iterator(); i.hasNext();) {
                    EJBInstanceContext context = (EJBInstanceContext) i.next();
                    if (!context.isDead()) {
                        context.beforeCommit();
                    }
                }
            }
            if (currentContext != null && currentContext.getId() != null) {
                dirtyContexts.put(currentContext.getContainerId(), currentContext.getId(), currentContext);
            }
            if(cmpTxData != null) {
                try {
                    cmpTxData.flush();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException("Error flushing cmp cache", e);
                }
            }
        } catch (Throwable throwable) {
            log.error("Exception occured during ejbStore()", throwable);
            try {
                transaction.setRollbackOnly();
            } catch (javax.transaction.SystemException se) {
                log.error("Transaction manager reported error during setRollbackOnly()", throwable);
            }
        }
    }

    private void afterCommit(boolean status) {
        try {
            Throwable firstThrowable = null;
            ArrayList toFlush = getAssociatedContexts();
            for (Iterator i = toFlush.iterator(); i.hasNext();) {
                EJBInstanceContext context = (EJBInstanceContext) i.next();
                if (!context.isDead()) {
                    try {
                        context.afterCommit(status);
                    } catch (Throwable e) {
                        if (firstThrowable == null) {
                            firstThrowable = e;
                        }
                    }
                }
            }

            if (firstThrowable instanceof Error) {
                throw (Error) firstThrowable;
            } else if (firstThrowable instanceof RuntimeException) {
                throw (RuntimeException) firstThrowable;
            } else if (firstThrowable != null) {
                throw new RuntimeException("Unexpected throwable from afterCommit", firstThrowable);
            }
        } finally {
            remove(transaction);
            unassociateAll();
        }
    }

    private void unassociateAll() {
        ArrayList toFlush = getAssociatedContexts();
        for (Iterator i = toFlush.iterator(); i.hasNext();) {
            EJBInstanceContext context = (EJBInstanceContext) i.next();
            try {
                context.unassociate();
            } catch (Throwable throwable) {
                log.warn("Error while unassociating instance from transaction context: " + context, throwable);
            }
        }
    }

    private static class EjbSynchronization implements Synchronization {
        private final EjbTransactionContext ctx;

        public EjbSynchronization(EjbTransactionContext ctx) {
            this.ctx = ctx;
        }

        public void beforeCompletion() {
            ctx.beforeCompletion();
        }

        public void afterCompletion(int status) {
            ctx.afterCommit(status == Status.STATUS_COMMITTED);
        }

    }

    private ArrayList getAssociatedContexts() {
        return new ArrayList(associatedContexts.values());
    }


}
