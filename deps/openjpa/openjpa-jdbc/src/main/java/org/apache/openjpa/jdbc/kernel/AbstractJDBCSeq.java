/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.StoreException;

/**
 * Abstract sequence implementation. Handles obtaining the proper
 * connection to used based on whether the sequence is transactional and
 * whether a second datasource is configured.
 *
 * @author Abe White
 */
public abstract class AbstractJDBCSeq
    implements JDBCSeq {

    protected int type = TYPE_DEFAULT;
    protected Object current = null;

    /**
     * Records the sequence type.
     */
    public void setType(int type) {
        this.type = type;
    }

    public Object next(StoreContext ctx, ClassMetaData meta) {
        JDBCStore store = getStore(ctx);
        try {
            Object currentLocal = nextInternal(store, (ClassMapping) meta);
            current = currentLocal;
            return currentLocal;
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    public Object current(StoreContext ctx, ClassMetaData meta) {
        JDBCStore store = getStore(ctx);
        try {
            return currentInternal(store, (ClassMapping) meta);
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    public void allocate(int additional, StoreContext ctx, ClassMetaData meta) {
        JDBCStore store = getStore(ctx);
        try {
            allocateInternal(additional, store, (ClassMapping) meta);
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, store.getDBDictionary());
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    /**
     * No-op.
     */
    public void addSchema(ClassMapping mapping, SchemaGroup group) {
    }

    /**
     * No-op.
     */
    public void close() {
    }

    /**
     * Return the next sequence object.
     */
    protected abstract Object nextInternal(JDBCStore store,
        ClassMapping mapping)
        throws Exception;
    
    /**
     * Return the {@link JDBCConfiguration} for this sequence.
     */
    public abstract JDBCConfiguration getConfiguration();

    /**
     * Return the current sequence object. By default returns the last
     * sequence value used, or null if no sequence values have been requested
     * yet. Default implementation is not threadsafe.
     */
    protected Object currentInternal(JDBCStore store, ClassMapping mapping)
        throws Exception {
        return current;
    }

    /**
     * Allocate additional sequence values. Does nothing by default.
     */
    protected void allocateInternal(int additional, JDBCStore store,
        ClassMapping mapping)
        throws Exception {
    }

    /**
     * Extract the store from the given context.
     */
    private JDBCStore getStore(StoreContext ctx) {
        return (JDBCStore) ctx.getStoreManager().getInnermostDelegate();
    }

    /**
     * <P>Return the connection to use based on the type of sequence. This
     * connection will automatically be closed; do not close it.</P>
     * 
     * @return If the sequence type is <code>TYPE_TRANSACTIONAL</code> or 
     * <code>TYPE_CONTIGUOUS</code> the connection from the {@link StoreManager}
     * will be returned. 
     * 
     * <P>Otherwise a new connection will be obtained using DataSource2 from the
     * current configuration. In this case autocommit is set to false prior to 
     * returning the connection.</P>
     */
    protected Connection getConnection(JDBCStore store)
        throws SQLException {
        if (type == TYPE_TRANSACTIONAL || type == TYPE_CONTIGUOUS) {
            // Also increments ref count.
            return store.getConnection();
        }
        else {
            JDBCConfiguration conf = store.getConfiguration();
            DataSource ds = conf.getDataSource2(store.getContext());
            Connection conn = ds.getConnection();
            if (conn.getAutoCommit())
                conn.setAutoCommit(false);
            return conn;
        }
    }

    /**
     * Close the current connection. If the sequence is
     * <code>TYPE_TRANSACTIONAL</code> or <code>TYPE_CONTIGUOUS</code>
     * we will decrement the ref count. Otherwise the connection will be
     * committed and then closed. 
     */
    protected void closeConnection(Connection conn) {
        if (conn == null)
            return;
        if (type == TYPE_TRANSACTIONAL || type == TYPE_CONTIGUOUS) {
            // The seq is part of the business transaction however we need
            // to decrement the ref count so that the connection may be 
            // closed appropriately.
          	try { 
           		conn.close();
           	}
           	catch(SQLException se) { 
           		throw SQLExceptions.getStore(se);
           	}
            return;
        }
        else {
            try {
                conn.commit();
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se);
            } finally {
                try { conn.close(); } catch (SQLException se) {}
            }
        }
    }
    
    /**
     * Detect whether or not OpenJPA should suspend the transaction in 
     * a managed environment.
     */
    protected boolean suspendInJTA() {
        return getConfiguration().isConnectionFactoryModeManaged() && 
            getConfiguration().getConnectionFactory2() == null;
    }
}
