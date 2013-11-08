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

import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.util.StoreException;

/**
 * Abstract provider implementation wrapped around a {@link Select}.
 *
 * @author Abe White
 * @nojavadoc
 */
public abstract class SelectResultObjectProvider
    implements ResultObjectProvider {

    private final SelectExecutor _sel;
    private final JDBCStore _store;
    private final JDBCFetchConfiguration _fetch;
    protected Result _res = null;
    private int _size = -1;
    private Boolean _ra = null;

    /**
     * Constructor.
     *
     * @param sel the select to execute
     * @param store the store to delegate loading to
     * @param fetch the fetch configuration, or null for the default
     */
    public SelectResultObjectProvider(SelectExecutor sel, JDBCStore store,
        JDBCFetchConfiguration fetch) {
        _sel = sel;
        _store = store;
        _fetch = fetch;
    }

    public SelectExecutor getSelect() {
        return _sel;
    }

    public JDBCStore getStore() {
        return _store;
    }

    public JDBCFetchConfiguration getFetchConfiguration() {
        return _fetch;
    }

    public Result getResult() {
        return _res;
    }

    public boolean supportsRandomAccess() {
        if (_ra == null) {
            boolean ra;
            if (_res != null) {
                try {
                    ra = _res.supportsRandomAccess();
                } catch (SQLException se) {
                    throw SQLExceptions.getStore(se, _store.getDBDictionary());
                }
            } else
                ra = _sel.supportsRandomAccess(_fetch.getReadLockLevel() > 0);
            _ra = (ra) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _ra.booleanValue();
    }

    public void open()
        throws SQLException {
        _res = _sel.execute(_store, _fetch);
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
        if (_size == -1) {
            // if res is null, don't cache size
            if (_res == null)
                return Integer.MAX_VALUE;

            switch (_fetch.getLRSSize()) {
                case LRSSizes.SIZE_UNKNOWN:
                    _size = Integer.MAX_VALUE;
                    break;
                case LRSSizes.SIZE_LAST:
                    if (supportsRandomAccess())
                        _size = _res.size();
                    else
                        _size = Integer.MAX_VALUE;
                    break;
                default: // query
                    _size = _sel.getCount(_store);
            }
        }
        return _size;
    }

    /**
     * Allow subclasses that know the size to set it; otherwise we calculate
     * it internally.
     */
    protected void setSize(int size) {
        if (_size == -1)
            _size = size;
    }

    public void reset()
        throws SQLException {
        close();
        open();
    }

    public void close() {
        if (_res != null) {
            _res.close();
            _res = null;
        }
    }

    public void handleCheckedException(Exception e) {
        if (e instanceof SQLException)
            throw SQLExceptions.getStore((SQLException) e, _store.getDBDictionary(), _fetch.getReadLockLevel());
        throw new StoreException(e);
    }
}
