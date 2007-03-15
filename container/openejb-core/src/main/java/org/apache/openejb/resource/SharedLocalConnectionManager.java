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
import java.util.Map;
import java.util.Collections;
import java.util.Properties;
import java.io.Serializable;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.ResourceException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.Status;

import org.apache.openejb.resource.jdbc.JdbcManagedConnectionFactory;
import org.apache.openejb.resource.jdbc.JdbcUnmanagedConnection;

/**
 * @org.apache.xbean.XBean element="sharedLocalConnectionManager"
 */
public class SharedLocalConnectionManager implements ConnectionManager, ConnectionEventListener, Serializable {
    private static final long serialVersionUID = -276853822988761008L;

    private final ThreadLocal<ConnectionCache> threadConnectionCache = new ThreadLocal<ConnectionCache>() {
        protected ConnectionCache initialValue() {
            return new ConnectionCache();
        }
    };
    private final Set<ManagedConnection> connSet = Collections.synchronizedSet(new HashSet<ManagedConnection>());
    private TransactionManager transactionManager;

    public SharedLocalConnectionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void init(Properties props) {
        transactionManager = (TransactionManager) props.get(TransactionManager.class.getName());
    }

    public Object allocateConnection(ManagedConnectionFactory factory, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        ConnectionCache connectionCache = null;
        ManagedConnection conn = null;
        if (!(factory instanceof JdbcManagedConnectionFactory) || !((JdbcManagedConnectionFactory) factory).isUnmanaged()) {
            connectionCache = threadConnectionCache.get();
            conn = connectionCache.getConnection(factory);
        }
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
            } catch (SystemException se) {
                throw new ApplicationServerInternalException("Can not obtain a Transaction object from TransactionManager. " + se.getMessage());
            } catch (RollbackException re) {
                throw new ApplicationServerInternalException("Can not register org.apache.openejb.resource.LocalTransacton with transaciton manager. Transaction has already been rolled back" + re.getMessage());
            }

            if (connectionCache != null) {
                connectionCache.putConnection(factory, conn);
            }
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
        } catch (SystemException se) {

        } catch (ResourceException re) {

        }
    }

    public void connectionErrorOccurred(ConnectionEvent event) {
        ManagedConnection conn = (ManagedConnection) event.getSource();

        try {
            conn.destroy();

            threadConnectionCache.get().removeConnection(conn);
        } catch (ResourceException re) {

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
            try {
                conn.cleanup();
                if (!(conn instanceof JdbcUnmanagedConnection)) {
                    connSet.add(conn);
                }
            } catch (ResourceException re) {
                try {
                    conn.destroy();
                } catch (ResourceException re2) {
                }
            }

            threadConnectionCache.get().removeConnection(conn);
        }
    }

    public void localTransactionStarted(ConnectionEvent event) {
    }

    static class Synchronizer implements Synchronization {
        LocalTransaction localTx;

        public Synchronizer(LocalTransaction lt) {
            localTx = lt;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
                try {
                    localTx.commit();
                } catch (ResourceException re) {
                    throw new RuntimeException("JDBC driver failed to commit transaction. " + re.getMessage());
                }
            } else {
                try {
                    localTx.rollback();
                } catch (ResourceException re) {
                    throw new RuntimeException("JDBC driver failed to rollback transaction. " + re.getMessage());
                }
            }
        }
    }

    /**
     * Thread scoped cache of connections.
     */
    private static class ConnectionCache {
        private final Map<ManagedConnectionFactory,ManagedConnection> connectionByFactory =
                new HashMap<ManagedConnectionFactory,ManagedConnection>();
        private final Map<ManagedConnection,ManagedConnectionFactory> factoriesByConnection = new
                HashMap<ManagedConnection,ManagedConnectionFactory>();

        public ManagedConnection getConnection(ManagedConnectionFactory factory) {
            return connectionByFactory.get(factory);
        }

        public void putConnection(ManagedConnectionFactory factory, ManagedConnection connection) {
            connectionByFactory.put(factory, connection);
            factoriesByConnection.put(connection, factory);
        }

        public void removeConnection(ManagedConnection connection) {
            ManagedConnectionFactory factory = factoriesByConnection.remove(connection);
            if (factory != null) {
                connectionByFactory.remove(factory);
            }
        }
    }
}