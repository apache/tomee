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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Object provider implementation wrapped around a generic {@link Result}.
 *
 * @author Abe White
 */
public class GenericResultObjectProvider
    implements ResultObjectProvider {

    private final ClassMapping _mapping;
    private final JDBCStore _store;
    private final JDBCFetchConfiguration _fetch;
    private final Result _res;

    /**
     * Constructor.
     *
     * @param pcClass the base class of the result objects
     * @param store the store manager to delegate loading to
     * @param fetch the fetch configuration, or null for default
     * @param res the result containing the data
     */
    public GenericResultObjectProvider(Class<?> pcClass,
        JDBCStore store, JDBCFetchConfiguration fetch, Result res) {
        this(store.getConfiguration().getMappingRepositoryInstance().getMapping
            (pcClass, store.getContext().getClassLoader(), true),
            store, fetch, res);
    }

    /**
     * Constructor.
     *
     * @param mapping the mapping for the base class of the result objects
     * @param store the store manager to delegate loading to
     * @param fetch the fetch configuration, or null for default
     * @param res the result containing the data
     */
    public GenericResultObjectProvider(ClassMapping mapping,
        JDBCStore store, JDBCFetchConfiguration fetch, Result res) {
        _mapping = mapping;
        _store = store;
        if (fetch == null)
            _fetch = store.getFetchConfiguration();
        else
            _fetch = fetch;
        _res = res;
    }

    public boolean supportsRandomAccess() {
        try {
            return _res.supportsRandomAccess();
        } catch (Throwable t) {
            return false;
        }
    }

    public void open() {
    }

    public Object getResultObject()
        throws SQLException {
        // rather than use the standard result.load(), we go direct to
        // the store manager so we can tell it not to load anything additional
        return ((JDBCStoreManager) _store).load(_mapping, _fetch,
            StoreContext.EXCLUDE_ALL, _res);
    }

    public boolean next()
        throws SQLException {
        return _res.next();
    }

    public boolean absolute(int pos)
        throws SQLException {
        return _res.absolute(pos);
    }

    public int size()
        throws SQLException {
        if (_fetch.getLRSSize() == LRSSizes.SIZE_UNKNOWN
            || !supportsRandomAccess())
            return Integer.MAX_VALUE;
        return _res.size();
    }

    public void reset() {
        throw new UnsupportedException();
    }

    public void close() {
        _res.close();
    }

    public void handleCheckedException(Exception e) {
        if (e instanceof SQLException)
            throw SQLExceptions.getStore((SQLException) e, _store.getDBDictionary(), _fetch.getReadLockLevel());
        throw new StoreException(e);
    }
}
