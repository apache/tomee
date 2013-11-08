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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.LogicalUnion;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.jdbc.sql.SelectImpl;
import org.apache.openjpa.jdbc.sql.Union;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.FinderQuery;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.Id;


/**
 * Implements Finder Query identified by ClassMappping for SelectExecutor that 
 * can be executed to generate Result. 
 *  
 * @author Pinaki Poddar
 *
 * @since 2.0.0
 */
public class FinderQueryImpl 
    implements FinderQuery<ClassMapping, SelectExecutor, Result> {
    private final ClassMapping _mapping;
    private final SelectImpl _select;
    private final Column[] _pkCols;
    private final Joinable[]  _joins;
    private final int[] _pkIndices;
    private final SQLBuffer _buffer;
    private final String _sql;
    
    /**
     * Attempts to construct a FinderQuery from the given Select for the given
     * mapping. The given Select may not be amenable for caching and then a null
     * value is returned. 
     */
    static FinderQueryImpl newFinder(ClassMapping mapping,
        SelectExecutor select) {
        SelectImpl impl = extractImplementation(select);
        if (impl == null)
            return null;
        SQLBuffer buffer = impl.getSQL();
        Column[] pkCols = mapping.getPrimaryKeyColumns();
        boolean canCache = pkCols.length == buffer.getParameters().size();
        return (canCache)
            ? new FinderQueryImpl(mapping, impl, buffer) : null;
    }

    private FinderQueryImpl(ClassMapping mapping, SelectImpl select,
        SQLBuffer buffer) {
        super();
        _mapping = mapping;
        _select = select;
        _buffer = buffer;
        _sql = _buffer.getSQL();
        _pkCols = _mapping.getPrimaryKeyColumns();
        _joins = new Joinable[_pkCols.length];
        for (int i = 0; i < _pkCols.length; i++)
            _joins[i] = _mapping.assertJoinable(_pkCols[i]);
        _pkIndices = new int[_pkCols.length];
        for (int i = 0; i < _pkCols.length; i++) {
            FieldMetaData pk = _mapping.getField(_joins[i].getFieldIndex());
            _pkIndices[i] = pk == null ? 0 : pk.getPrimaryKeyIndex();
        }
        
    }
    
    public ClassMapping getIdentifier() {
        return _mapping;
    }
    
    public SelectExecutor getDelegate() {
        return _select;
    }
    
    public String getQueryString() {
        return _sql;
    }
    
    public Column[] getPKColumns() {
        return _pkCols;
    }
    
    private Object[] getPKValues(OpenJPAStateManager sm, JDBCStore store) {
        Object[] pks = null;
        Object oid = sm.getObjectId();
        if (_mapping.getIdentityType() == ClassMapping.ID_APPLICATION)
            pks = ApplicationIds.toPKValues(oid, _mapping);
    
        Object[] val = new Object[_pkCols.length];
        int count = 0;
        for (int i = 0; i < _pkCols.length; i++, count++) {
            if (pks == null)
                val[0] = (oid == null) 
                    ? null : ((Id) oid).getId();
            else {
                val[i] = _joins[i].getJoinValue(pks[_pkIndices[i]], _pkCols[i], 
                    store);
            }
        }
        return val;
    }
    
    public Result execute(OpenJPAStateManager sm, StoreManager store, 
        FetchConfiguration fetch) {
        boolean forUpdate = false;
        JDBCStore jstore = (JDBCStore)store;
        Connection conn = jstore.getConnection();
        DBDictionary dict = jstore.getDBDictionary();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = _select.prepareStatement(conn, _sql);
            Object[] params = getPKValues(sm, jstore);
            if (stmnt != null) {
                for (int i = 0; i <params.length; i++) {
                    dict.setUnknown(stmnt, i+1, params[i], _pkCols[i]);
                }
            }
            dict.setTimeouts(stmnt, (JDBCFetchConfiguration)fetch, forUpdate);
            rs = _select.executeQuery(conn, stmnt, getQueryString(), jstore,
                params, _pkCols);
            return _select.getEagerResult(conn, stmnt, rs, jstore, 
                (JDBCFetchConfiguration)fetch, forUpdate, _buffer);
        } catch (SQLException se) {
            if (stmnt != null)
                try { stmnt.close(); } catch (SQLException se2) {}
            try { conn.close(); } catch (SQLException se2) {}
            throw new RuntimeException(se);
        }
    }
    
    private static SelectImpl extractImplementation(SelectExecutor selector) {
        if (selector == null || selector.hasMultipleSelects())
            return null;
        if (selector instanceof SelectImpl) 
            return (SelectImpl)selector;
        if (selector instanceof LogicalUnion.UnionSelect)
            return ((LogicalUnion.UnionSelect)selector).getDelegate();
        if (selector instanceof Union) 
            return extractImplementation(((Union)selector).getSelects()[0]);
        
        return null;
    }
    
    public String toString() {
        return _mapping + ": [" + getQueryString() + "]";
    }
}
