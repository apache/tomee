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
package org.apache.openjpa.kernel;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * Base class for store manager decorators that delegate to another
 * store manager for some operations.
 *
 * @author Abe White
 */
public abstract class DelegatingStoreManager
    implements StoreManager {

    private final StoreManager _store;
    private final DelegatingStoreManager _del;

    /**
     * Constructor. Supply delegate.
     */
    public DelegatingStoreManager(StoreManager store) {
        _store = store;
        if (store instanceof DelegatingStoreManager)
            _del = (DelegatingStoreManager) _store;
        else
            _del = null;
    }

    /**
     * Return the wrapped store manager.
     */
    public StoreManager getDelegate() {
        return _store;
    }

    /**
     * Return the base underlying native store manager.
     */
    public StoreManager getInnermostDelegate() {
        return (_del == null) ? _store : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingStoreManager)
            other = ((DelegatingStoreManager) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    public void setContext(StoreContext ctx) {
        _store.setContext(ctx);
    }

    public void beginOptimistic() {
        _store.beginOptimistic();
    }

    public void rollbackOptimistic() {
        _store.rollbackOptimistic();
    }

    public void begin() {
        _store.begin();
    }

    public void commit() {
        _store.commit();
    }

    public void rollback() {
        _store.rollback();
    }

    public boolean exists(OpenJPAStateManager sm, Object context) {
        return _store.exists(sm, context);
    }

    public boolean syncVersion(OpenJPAStateManager sm, Object context) {
        return _store.syncVersion(sm, context);
    }

    public boolean initialize(OpenJPAStateManager sm, PCState state,
        FetchConfiguration fetch, Object context) {
        return _store.initialize(sm, state, fetch, context);
    }

    public boolean load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, int lockLevel, Object context) {
        return _store.load(sm, fields, fetch, lockLevel, context);
    }

    public Collection<Object> loadAll(Collection<OpenJPAStateManager> sms, PCState state, int load,
        FetchConfiguration fetch, Object context) {
        return _store.loadAll(sms, state, load, fetch, context);
    }

    public void beforeStateChange(OpenJPAStateManager sm, PCState fromState,
        PCState toState) {
        _store.beforeStateChange(sm, fromState, toState);
    }

    public Collection<Exception> flush(Collection<OpenJPAStateManager> sms) {
        return _store.flush(sms);
    }

    public boolean assignObjectId(OpenJPAStateManager sm, boolean preFlush) {
        return _store.assignObjectId(sm, preFlush);
    }

    public boolean assignField(OpenJPAStateManager sm, int field,
        boolean preFlush) {
        return _store.assignField(sm, field, preFlush);
    }

    public Class<?> getManagedType(Object oid) {
        return _store.getManagedType(oid);
    }

    public Class<?> getDataStoreIdType(ClassMetaData meta) {
        return _store.getDataStoreIdType(meta);
    }

    public Object copyDataStoreId(Object oid, ClassMetaData meta) {
        return _store.copyDataStoreId(oid, meta);
    }

    public Object newDataStoreId(Object oidVal, ClassMetaData meta) {
        return _store.newDataStoreId(oidVal, meta);
    }

    public Object getClientConnection() {
        return _store.getClientConnection();
    }

    public void retainConnection() {
        _store.retainConnection();
    }

    public void releaseConnection() {
        _store.releaseConnection();
    }

    public ResultObjectProvider executeExtent(ClassMetaData meta,
        boolean subclasses, FetchConfiguration fetch) {
        return _store.executeExtent(meta, subclasses, fetch);
    }

    public StoreQuery newQuery(String language) {
        return _store.newQuery(language);
    }

    public FetchConfiguration newFetchConfiguration() {
        return _store.newFetchConfiguration();
    }

    public void close() {
        _store.close();
    }

    public int compareVersion(OpenJPAStateManager state, Object v1, Object v2) {
        return _store.compareVersion(state, v1, v2);
    }

    public Seq getDataStoreIdSequence(ClassMetaData forClass) {
        return _store.getDataStoreIdSequence(forClass);
    }

    public Seq getValueSequence(FieldMetaData fmd) {
        return _store.getValueSequence(fmd);
    }

    public boolean cancelAll() {
        return _store.cancelAll();
	}
	
    public boolean isCached(List<Object> oids, BitSet edata) {
        return _store.isCached(oids, edata);
    }
    
}
