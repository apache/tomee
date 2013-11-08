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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.Strategy;
import org.apache.openjpa.jdbc.meta.strats.ContainerFieldStrategy;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SQLFactory;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.LockScopes;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.VersionLockManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.LockException;
import org.apache.openjpa.util.StoreException;

/**
 * Lock manager that uses exclusive database locks.
 *
 * @author Marc Prud'hommeaux
 */
public class PessimisticLockManager
    extends VersionLockManager
    implements JDBCLockManager {

    public static final int LOCK_DATASTORE_ONLY = 1;

    private static final Localizer _loc = Localizer.forPackage
        (PessimisticLockManager.class);

    protected JDBCStore _store;

    public PessimisticLockManager() {
        setVersionCheckOnReadLock(false);
        setVersionUpdateOnWriteLock(false);
    }

    public void setContext(StoreContext ctx) {
        super.setContext(ctx);
        _store = (JDBCStore) ctx.getStoreManager().getInnermostDelegate();
    }

    public boolean selectForUpdate(Select sel, int lockLevel) {
        if (lockLevel == LOCK_NONE)
            return false;

        DBDictionary dict = _store.getDBDictionary();
        if (dict.simulateLocking)
            return false;
        dict.assertSupport(dict.supportsSelectForUpdate,
            "SupportsSelectForUpdate");

        if (!sel.supportsLocking()) {
            if (log.isInfoEnabled())
                log.info(_loc.get("cant-lock-on-load",
                    sel.toSelect(false, null).getSQL()));
            return false;
        }

        ensureStoreManagerTransaction();
        return true;
    }

    public void loadedForUpdate(OpenJPAStateManager sm) {
        // we set a low lock level to indicate that we don't need datastore
        // locking, but we don't necessarily have a read or write lock
        // according to our superclass
        if (getLockLevel(sm) == LOCK_NONE)
            setLockLevel(sm, LOCK_DATASTORE_ONLY);
    }

    protected void lockInternal(OpenJPAStateManager sm, int level, int timeout,
        Object sdata, boolean postVersionCheck) {
        // we can skip any already-locked instance regardless of level because
        // we treat all locks the same (though super doesn't).
        
        // only need to lock if not loaded from locking result
        ConnectionInfo info = (ConnectionInfo) sdata;
        if (info == null || info.result == null || !info.result.isLocking())
            lockRow(sm, timeout, level);
        optimisticLockInternal(sm, level, timeout, sdata, postVersionCheck);
    }

    /**
     * Lock the specified instance row by issuing a "SELECT ... FOR UPDATE"
     * statement.
     */
    private void lockRow(OpenJPAStateManager sm, int timeout, int level) {
        // assert that the dictionary supports the "SELECT ... FOR UPDATE"
        // construct; if not, and we the assertion does not throw an
        // exception, then just return without locking
        DBDictionary dict = _store.getDBDictionary();
        JDBCFetchConfiguration fetch = _store.getFetchConfiguration();
        if (dict.simulateLocking)
            return;
        dict.assertSupport(dict.supportsSelectForUpdate, "SupportsSelectForUpdate");

        Object id = sm.getObjectId();
        ClassMapping mapping = (ClassMapping) sm.getMetaData();

        List<SQLBuffer> sqls = sm.getLock() == null
            ?  getLockRows(dict, id, mapping, fetch, _store.getSQLFactory())
            : new ArrayList<SQLBuffer>();
        if (ctx.getFetchConfiguration().getLockScope() == LockScopes.LOCKSCOPE_EXTENDED)
            lockJoinTables(sqls, dict, id, mapping, fetch, _store.getSQLFactory());

        ensureStoreManagerTransaction();
        Connection conn = _store.getConnection();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            for (SQLBuffer sql : sqls) {
                stmnt = prepareStatement(conn, sql);
                dict.setTimeouts(stmnt, fetch, true);
                rs = executeQuery(conn, stmnt, sql);
                checkLock(rs, sm, timeout);
            }
        } catch (SQLException se) {
            LockException e = new LockException(sm.getPersistenceCapable(), timeout, level);
            e.setCause(se);
            e.setFatal(dict.isFatalException(StoreException.LOCK, se) 
                    || level >= MixedLockLevels.LOCK_PESSIMISTIC_READ);
            throw e;
        } finally {
            if (stmnt != null)
                try { stmnt.close(); } catch (SQLException se) {}
            if (rs != null)
                try { rs.close(); } catch (SQLException se) {}
            try { conn.close(); } catch (SQLException se) {}
        }
    }

    protected List<SQLBuffer> getLockRows(DBDictionary dict, Object id, ClassMapping mapping,
            JDBCFetchConfiguration fetch, SQLFactory factory) {
        while (mapping.getJoinablePCSuperclassMapping() != null)
            mapping = mapping.getJoinablePCSuperclassMapping();
        // select only the PK columns, since we just want to lock
        Select select = factory.newSelect();
        select.select(mapping.getPrimaryKeyColumns());
        select.wherePrimaryKey(id, mapping, _store);
        List<SQLBuffer> sqls = new ArrayList<SQLBuffer>();
        sqls.add(select.toSelect(true, fetch));
        return sqls;
    }
    
    protected void lockJoinTables(List<SQLBuffer> sqls, DBDictionary dict, Object id, ClassMapping mapping,
            JDBCFetchConfiguration fetch, SQLFactory factory) {
        FieldMapping[] fms = mapping.getFieldMappings();
        for (int i = 0; i < fms.length; i++) {
            Strategy strat = fms[i].getStrategy();
            if (strat instanceof ContainerFieldStrategy) {
                ForeignKey fk = ((ContainerFieldStrategy)strat).getJoinForeignKey();
                Select select = factory.newSelect();
                select.select(fk.getColumns());
                select.whereForeignKey(fk, id, fms[i].getDefiningMapping(), _store);
                sqls.add(select.toSelect(true, fetch));
            }
        }
    }

    /**
     * Enforce that we have an actual transaction in progress so that we can
     * start locking. The transaction should already be begun when using a
     * datastore transaction; this will just be used if we are locking in
     * optimistic mode.
     */
    private void ensureStoreManagerTransaction() {
        if (!_store.getContext().isStoreActive()) {
            _store.getContext().beginStore();
            if (log.isTraceEnabled())
                log.trace(_loc.get("start-trans-for-lock"));
        }
    }
    
    public JDBCStore getStore() {
        return _store;
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of preparing statement.
     */
    protected PreparedStatement prepareStatement(Connection conn, SQLBuffer sql)
        throws SQLException {
        return sql.prepareStatement(conn);
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of executing query.
     */
    protected ResultSet executeQuery(Connection conn, PreparedStatement stmnt, 
        SQLBuffer sql) throws SQLException {
        return stmnt.executeQuery();
    }
    
    /**
     * This method is to provide override for non-JDBC or JDBC-like 
     * implementation of checking lock from the result set.
     */
    protected void checkLock(ResultSet rs, OpenJPAStateManager sm, int timeout)
        throws SQLException { 
        if (!rs.next())
            throw new LockException(sm.getManagedInstance(), timeout);
        return;
    }
}
