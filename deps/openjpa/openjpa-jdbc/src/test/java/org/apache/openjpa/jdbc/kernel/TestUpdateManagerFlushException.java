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

import java.io.IOException;
import java.io.ObjectOutput;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Strategy;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLFactory;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.Id;

/**
 * <p>
 * Tests AbstractUpdateManager flush's method exception return behavior.
 * </p>
 * 
 * ================  IMPORTANT NOTE ======================================
 * This test is retired temporarily. This test declares a TestConnection 
 * class which needs to be abstract for JDK6/JDBC4. 
 * =======================================================================
 * 
 * @author Albert Lee
 */

public class TestUpdateManagerFlushException extends /* Abstract */TestCase {

    private TestUpdateManager updMgr;

    public void setUp() {
        updMgr = new TestUpdateManager();
    }
    
    public void testDummy() {
        
    }

    /**
     * Tests exception collection returns from UpdateManager flush method is in
     * the order the original exceptions are thrown.
     */
    public void xtestAddRetrieve() {
        
        Collection states = new ArrayList<OpenJPAStateManager>();
        states.add(new TestOpenJPAStateManager());

        Collection exceps = updMgr.flush(states, new TestJDBCStore());

        assertEquals(3, exceps.size());
        
        Iterator<Exception> itr = exceps.iterator();
        assertEquals(itr.next().getMessage(),
            "TestUpdateManager.populateRowManager");
        assertEquals(itr.next().getMessage(),
            "TestUpdateManager.flush");
        assertEquals(itr.next().getMessage(),
            "TestUpdateManager.customInsert");
    }

    /*
     * Scaffolding test update manager.
     */
    class TestUpdateManager extends AbstractUpdateManager {

        protected Collection flush(RowManager rowMgr,
            PreparedStatementManager psMgr, Collection exceps) {

            exceps.add(new SQLException("TestUpdateManager.flush"));

            return exceps;
        }

        protected PreparedStatementManager newPreparedStatementManager(
            JDBCStore store, Connection conn) {
            return new PreparedStatementManagerImpl(store, conn);
        }

        protected RowManager newRowManager() {
            return null;
        }

        public boolean orderDirty() {
            return false;
        }

        protected Collection populateRowManager(OpenJPAStateManager sm,
            RowManager rowMgr, JDBCStore store, Collection exceps,
            Collection customs) {
            
            exceps.add(new SQLException(
                "TestUpdateManager.populateRowManager"));
            customs.add(new CustomMapping(CustomMapping.INSERT, sm,
                new Strategy() {
                    public void customDelete(OpenJPAStateManager sm,
                        JDBCStore store) throws SQLException {
                    }

                    public void customInsert(OpenJPAStateManager sm,
                        JDBCStore store) throws SQLException {
                        throw new SQLException(
                            "TestUpdateManager.customInsert");
                    }

                    public void customUpdate(OpenJPAStateManager sm,
                        JDBCStore store) throws SQLException {
                    }

                    public void delete(OpenJPAStateManager sm, JDBCStore store,
                        RowManager rm) throws SQLException {
                    }

                    public String getAlias() {
                        return null;
                    }

                    public void initialize() {
                    }

                    public void insert(OpenJPAStateManager sm, JDBCStore store,
                        RowManager rm) throws SQLException {

                    }

                    public Boolean isCustomDelete(OpenJPAStateManager sm,
                        JDBCStore store) {
                        return null;
                    }

                    public Boolean isCustomInsert(OpenJPAStateManager sm,
                        JDBCStore store) {
                        return null;
                    }

                    public Boolean isCustomUpdate(OpenJPAStateManager sm,
                        JDBCStore store) {
                        return null;
                    }

                    public void map(boolean adapt) {
                    }

                    public void update(OpenJPAStateManager sm, JDBCStore store,
                        RowManager rm) throws SQLException {
                    }
                }));
            return exceps;
        }
    }

    /*
     * Scaffolding test state manager.
     */
    class TestOpenJPAStateManager implements OpenJPAStateManager {

        public boolean assignObjectId(boolean flush) {
            return false;
        }

        public boolean beforeRefresh(boolean refreshAll) {
            return false;
        }

        public void dirty(int field) {
        }

        public Object fetch(int field) {
            return null;
        }

        public boolean fetchBoolean(int field) {
            return false;
        }

        public byte fetchByte(int field) {
            return 0;
        }

        public char fetchChar(int field) {
            return 0;
        }

        public double fetchDouble(int field) {
            return 0;
        }

        public Object fetchField(int field, boolean transitions) {
            return null;
        }

        public float fetchFloat(int field) {
            return 0;
        }

        public Object fetchInitialField(int field) {
            return null;
        }

        public int fetchInt(int field) {
            return 0;
        }

        public long fetchLong(int field) {
            return 0;
        }

        public Object fetchObject(int field) {
            return null;
        }

        public short fetchShort(int field) {
            return 0;
        }

        public String fetchString(int field) {
            return null;
        }

        public StoreContext getContext() {
            return null;
        }

        public BitSet getDirty() {
            return null;
        }

        public BitSet getFlushed() {
            return null;
        }

        public Object getId() {
            return null;
        }

        public Object getImplData() {
            return null;
        }

        public Object getImplData(int field) {
            return null;
        }

        public Object getIntermediate(int field) {
            return null;
        }

        public BitSet getLoaded() {
            return null;
        }

        public Object getLock() {
            return null;
        }

        public Object getManagedInstance() {
            return null;
        }

        public ClassMetaData getMetaData() {
            return null;
        }

        public Object getObjectId() {
            return null;
        }

        public OpenJPAStateManager getOwner() {
            return null;
        }

        public int getOwnerIndex() {
            return 0;
        }

        public PCState getPCState() {
            return null;
        }

        public PersistenceCapable getPersistenceCapable() {
            return null;
        }

        public BitSet getUnloaded(FetchConfiguration fetch) {
            return null;
        }

        public Object getVersion() {
            return null;
        }

        public void initialize(Class forType, PCState state) {
        }

        public boolean isDefaultValue(int field) {
            return false;
        }

        public boolean isEmbedded() {
            return false;
        }

        public boolean isFlushed() {
            return false;
        }

        public boolean isFlushedDirty() {
            return false;
        }

        public boolean isImplDataCacheable() {
            return false;
        }

        public boolean isImplDataCacheable(int field) {
            return false;
        }

        public boolean isProvisional() {
            return false;
        }

        public boolean isVersionCheckRequired() {
            return false;
        }

        public boolean isVersionUpdateRequired() {
            return false;
        }

        public void load(FetchConfiguration fetch) {
        }

        public Object newFieldProxy(int field) {
            return null;
        }

        public Object newProxy(int field) {
            return null;
        }

        public void removed(int field, Object removed, boolean key) {
        }

        public Object setImplData(Object data, boolean cacheable) {
            return null;
        }

        public Object setImplData(int field, Object data) {
            return null;
        }

        public void setIntermediate(int field, Object value) {
        }

        public void setLock(Object lock) {
        }

        public void setNextVersion(Object version) {
        }

        public void setObjectId(Object oid) {
        }

        public void setRemote(int field, Object value) {
        }

        public void setVersion(Object version) {
        }

        public void store(int field, Object value) {
        }

        public void storeBoolean(int field, boolean externalVal) {
        }

        public void storeByte(int field, byte externalVal) {
        }

        public void storeChar(int field, char externalVal) {
        }

        public void storeDouble(int field, double externalVal) {
        }

        public void storeField(int field, Object value) {
        }

        public void storeFloat(int field, float externalVal) {
        }

        public void storeInt(int field, int externalVal) {
        }

        public void storeLong(int field, long externalVal) {
        }

        public void storeObject(int field, Object externalVal) {
        }

        public void storeShort(int field, short externalVal) {
        }

        public void storeString(int field, String externalVal) {
        }

        public void accessingField(int idx) {
        }

        public void dirty(String field) {
        }

        public Object fetchObjectId() {
            return null;
        }

        public Object getGenericContext() {
            return null;
        }

        public Object getPCPrimaryKey(Object oid, int field) {
            return null;
        }

        public boolean isDeleted() {
            return false;
        }

        public boolean isDetached() {
            return false;
        }

        public boolean isDirty() {
            return false;
        }

        public boolean isNew() {
            return false;
        }

        public boolean isPersistent() {
            return false;
        }

        public boolean isTransactional() {
            return false;
        }

        public void providedBooleanField(PersistenceCapable pc, int idx,
            boolean cur) {
        }

        public void providedByteField(PersistenceCapable pc, int idx,
            byte cur) {
        }

        public void providedCharField(PersistenceCapable pc, int idx, 
            char cur) {
        }

        public void providedDoubleField(PersistenceCapable pc, int idx,
            double cur) {
        }

        public void providedFloatField(PersistenceCapable pc, int idx,
            float cur) {
        }

        public void providedIntField(PersistenceCapable pc, int idx, 
            int cur) {
        }

        public void providedLongField(PersistenceCapable pc, int idx, 
            long cur) {
        }

        public void providedObjectField(PersistenceCapable pc, int idx,
            Object cur) {
        }

        public void providedShortField(PersistenceCapable pc, int idx, 
            short cur) {
        }

        public void providedStringField(PersistenceCapable pc, int idx,
            String cur) {
        }

        public void proxyDetachedDeserialized(int idx) {
        }

        public boolean replaceBooleanField(PersistenceCapable pc, int idx) {
            return false;
        }

        public byte replaceByteField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public char replaceCharField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public double replaceDoubleField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public float replaceFloatField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public int replaceIntField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public long replaceLongField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public Object replaceObjectField(PersistenceCapable pc, int idx) {
            return null;
        }

        public short replaceShortField(PersistenceCapable pc, int idx) {
            return 0;
        }

        public StateManager replaceStateManager(StateManager sm) {
            return null;
        }

        public String replaceStringField(PersistenceCapable pc, int idx) {
            return null;
        }

        public boolean serializing() {
            return false;
        }

        public void settingBooleanField(PersistenceCapable pc, int idx,
            boolean cur, boolean next, int set) {
        }

        public void settingByteField(PersistenceCapable pc, int idx, byte cur,
            byte next, int set) {
        }

        public void settingCharField(PersistenceCapable pc, int idx, char cur,
            char next, int set) {
        }

        public void settingDoubleField(PersistenceCapable pc, int idx,
            double cur, double next, int set) {
        }

        public void settingFloatField(PersistenceCapable pc, int idx,
            float cur, float next, int set) {
        }

        public void settingIntField(PersistenceCapable pc, int idx, int cur,
            int next, int set) {
        }

        public void settingLongField(PersistenceCapable pc, int idx, long cur,
            long next, int set) {
        }

        public void settingObjectField(PersistenceCapable pc, int idx,
            Object cur, Object next, int set) {
        }

        public void settingShortField(PersistenceCapable pc, int idx,
            short cur, short next, int set) {
        }

        public void settingStringField(PersistenceCapable pc, int idx,
            String cur, String next, int set) {
        }

        public boolean writeDetached(ObjectOutput out) throws IOException {
            return false;
        }

        public void storeBooleanField(int fieldIndex, boolean value) {
        }

        public void storeByteField(int fieldIndex, byte value) {
        }

        public void storeCharField(int fieldIndex, char value) {
        }

        public void storeDoubleField(int fieldIndex, double value) {
        }

        public void storeFloatField(int fieldIndex, float value) {
        }

        public void storeIntField(int fieldIndex, int value) {
        }

        public void storeLongField(int fieldIndex, long value) {
        }

        public void storeObjectField(int fieldIndex, Object value) {
        }

        public void storeShortField(int fieldIndex, short value) {
        }

        public void storeStringField(int fieldIndex, String value) {
        }

        public boolean fetchBooleanField(int fieldIndex) {
            return false;
        }

        public byte fetchByteField(int fieldIndex) {
            return 0;
        }

        public char fetchCharField(int fieldIndex) {
            return 0;
        }

        public double fetchDoubleField(int fieldIndex) {
            return 0;
        }

        public float fetchFloatField(int fieldIndex) {
            return 0;
        }

        public int fetchIntField(int fieldIndex) {
            return 0;
        }

        public long fetchLongField(int fieldIndex) {
            return 0;
        }

        public Object fetchObjectField(int fieldIndex) {
            return null;
        }

        public short fetchShortField(int fieldIndex) {
            return 0;
        }

        public String fetchStringField(int fieldIndex) {
            return null;
        }

        @Override
        public boolean isDelayed(int field) {
            return false;
        }

        @Override
        public void setDelayed(int field, boolean delay) {
        }

        public void loadDelayedField(int field) {
        }
    }

    /*
     * Scaffolding test connection.
     */
    abstract class TestConnection implements Connection {

        public void clearWarnings() throws SQLException {
        }

        public void close() throws SQLException {
        }

        public void commit() throws SQLException {
        }

        public Statement createStatement() throws SQLException {
            return null;
        }

        public Statement createStatement(int resultSetType,
            int resultSetConcurrency) throws SQLException {
            return null;
        }

        public Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
            return null;
        }

        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        public String getCatalog() throws SQLException {
            return null;
        }

        public int getHoldability() throws SQLException {
            return 0;
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        public int getTransactionIsolation() throws SQLException {
            return 0;
        }

        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return null;
        }

        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        public boolean isClosed() throws SQLException {
            return false;
        }

        public boolean isReadOnly() throws SQLException {
            return false;
        }

        public String nativeSQL(String sql) throws SQLException {
            return null;
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            return null;
        }

        public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
            return null;
        }

        public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql)
            throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys) throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql,
            int[] columnIndexes) throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql,
            String[] columnNames) throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        public PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
            return null;
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        }

        public void rollback() throws SQLException {
        }

        public void rollback(Savepoint savepoint) throws SQLException {
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
        }

        public void setCatalog(String catalog) throws SQLException {
        }

        public void setHoldability(int holdability) throws SQLException {
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
        }

        public Savepoint setSavepoint() throws SQLException {
            return null;
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            return null;
        }

        public void setTransactionIsolation(int level) throws SQLException {
        }

        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        }
    }

    /*
     * Scaffolding test store manager.
     */
    class TestJDBCStore implements JDBCStore {

        public Object find(Object oid, ValueMapping vm,
            JDBCFetchConfiguration fetch) {
            return null;
        }

        public JDBCConfiguration getConfiguration() {
            return null;
        }

        public Connection getConnection() {
            throw new RuntimeException("TestConnection is abstract for JDK6");
//            return new TestConnection();
        }
        
        public Connection getNewConnection() {
            return getConnection();
        }

        public StoreContext getContext() {
            return null;
        }

        public DBDictionary getDBDictionary() {
            return null;
        }

        public JDBCFetchConfiguration getFetchConfiguration() {
            return null;
        }

        public JDBCLockManager getLockManager() {
            return null;
        }

        public SQLFactory getSQLFactory() {
            return null;
        }

        public void loadSubclasses(ClassMapping mapping) {

        }

        public Id newDataStoreId(long id, ClassMapping mapping, boolean subs) {
            return null;
        }
    }
}
