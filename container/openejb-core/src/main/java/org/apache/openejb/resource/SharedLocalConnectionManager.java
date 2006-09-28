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
package org.apache.openejb.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @org.apache.xbean.XBean element="sharedLocalConnectionManager"
 */
public class SharedLocalConnectionManager implements javax.resource.spi.ConnectionManager,
        javax.resource.spi.ConnectionEventListener,
        java.io.Serializable {

    private Set connSet;
    private SpecialHashThreadLocal threadLocal = new SpecialHashThreadLocal();
    private HashMap factoryMap = new HashMap();
    private TransactionManager transactionManager;

    public void init(java.util.Properties props) {
        transactionManager = (TransactionManager) props.get(TransactionManager.class.getName());
    }

    public SharedLocalConnectionManager() throws javax.resource.spi.ApplicationServerInternalException {
        connSet = java.util.Collections.synchronizedSet(new HashSet());
    }

    public java.lang.Object allocateConnection(ManagedConnectionFactory factory,
                                               ConnectionRequestInfo cxRequestInfo)
            throws javax.resource.ResourceException {

        ManagedConnection conn = (ManagedConnection) threadLocal.get(factory);
        if (conn == null) {
            conn = factory.matchManagedConnections(connSet, null, cxRequestInfo);
            if (conn != null) {
                connSet.remove(conn);
            } else {
                conn = factory.createManagedConnection(null, cxRequestInfo);
                conn.addConnectionEventListener(this);
            }
            conn.getLocalTransaction().begin();

            try {
                /*
                * The transaction manager has a  wrapper that ensures that any Synchronization
                * objects are handled after the EntityBean.ejbStore and SessionSynchronization methods of beans.
                * In the StatefulContainer and EntityContainer enterprise beans are wrapped
                * Synchronization wrappers, which must be handled
                * before the LocalTransaction objects in this connection manager.
                */
                Transaction tx = getTransactionManager().getTransaction();
                if (tx != null) {
                    tx.registerSynchronization(new Synchronizer(conn.getLocalTransaction()));
                }
            } catch (javax.transaction.SystemException se) {
                throw new javax.resource.spi.ApplicationServerInternalException("Can not obtain a Transaction object from TransactionManager. " + se.getMessage());
            } catch (javax.transaction.RollbackException re) {
                throw new javax.resource.spi.ApplicationServerInternalException("Can not register org.apache.openejb.resource.LocalTransacton with transaciton manager. Transaction has already been rolled back" + re.getMessage());
            }

            threadLocal.put(factory, conn);
        }

        Object handle = conn.getConnection(null, cxRequestInfo);
        return handle;
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void connectionClosed(ConnectionEvent event) {
        try {
            if (getTransactionManager().getTransaction() == null) {
                ManagedConnection conn = (ManagedConnection) event.getSource();
                conn.getLocalTransaction().commit();
                this.cleanup(conn);
            }
        } catch (javax.transaction.SystemException se) {

        } catch (javax.resource.ResourceException re) {

        }
    }

    public void connectionErrorOccurred(ConnectionEvent event) {
        ManagedConnection conn = (ManagedConnection) event.getSource();

        ManagedConnectionFactory mcf = (ManagedConnectionFactory) threadLocal.getKey(conn);
        try {
            conn.destroy();
            if (threadLocal.get(mcf) == conn) {
                threadLocal.put(mcf, null);
            }
        } catch (javax.resource.ResourceException re) {

        }
    }

    public void localTransactionCommitted(ConnectionEvent event) {
        cleanup((ManagedConnection) event.getSource());
    }

    public void localTransactionRolledback(ConnectionEvent event) {
        cleanup((ManagedConnection) event.getSource());
    }

    private void cleanup(ManagedConnection conn) {
        if (conn != null) {

            ManagedConnectionFactory mcf = (ManagedConnectionFactory) threadLocal.getKey(conn);
            try {
                conn.cleanup();
                connSet.add(conn);

            } catch (javax.resource.ResourceException re) {
                try {

                    conn.destroy();
                } catch (javax.resource.ResourceException re2) {

                }
            }
            threadLocal.put(mcf, null);
        }
    }

    public void localTransactionStarted(ConnectionEvent event) {

    }

    static class Synchronizer implements javax.transaction.Synchronization {
        LocalTransaction localTx;

        public Synchronizer(LocalTransaction lt) {
            localTx = lt;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == javax.transaction.Status.STATUS_COMMITTED) {
                try {
                    localTx.commit();
                } catch (javax.resource.ResourceException re) {
                    throw new RuntimeException("JDBC driver failed to commit transaction. " + re.getMessage());
                }
            } else {
                try {
                    localTx.rollback();
                } catch (javax.resource.ResourceException re) {
                    throw new RuntimeException("JDBC driver failed to rollback transaction. " + re.getMessage());
                }
            }
        }
    }

    /*
    * This class allows the ConnectionManager to determine the key used for
    * any object stored in this type of HashThreadLocal.  Its needed when handling
    * ConnectionListner events because the key (ManagedConnectionFactory) used to
    * store values (ManagedConnecitons) is not available.
    */
    static class SpecialHashThreadLocal extends org.apache.openejb.util.HashThreadLocal {
        HashMap keyMap = new HashMap();

        public synchronized void put(Object key, Object value) {
            if (!keyMap.containsKey(key)) {
                keyMap.put(value, key);
            }
            super.put(key, value);
        }

        public synchronized Object getKey(Object value) {
            return keyMap.get(value);
        }
    }

}