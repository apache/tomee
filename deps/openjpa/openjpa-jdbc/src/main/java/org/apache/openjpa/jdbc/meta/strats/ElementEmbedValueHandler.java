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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.*;
import java.util.*;

import org.apache.openjpa.lib.util.*;
import org.apache.openjpa.kernel.*;
import org.apache.openjpa.util.*;
import org.apache.openjpa.jdbc.meta.*;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.*;
import org.apache.openjpa.jdbc.schema.*;
import org.apache.openjpa.jdbc.sql.DBDictionary;

/**
 * <p>Handler for embedded objects as elements of a collection or map.  For
 * embedded objects as fields, use the more powerful
 * {@link EmbedFieldStrategy}.</p>
 *
 * @author Abe White
 * @since 0.4.0, 1.1.0
 * @nojavadoc
 */
public class ElementEmbedValueHandler
    extends EmbedValueHandler
    implements RelationId {

    private static final Localizer _loc = Localizer.forPackage
        (ElementEmbedValueHandler.class);

    private ValueMapping _vm = null;
    private Column[] _cols = null;
    private Object[] _args = null;
    private int _nullIdx = -1;
    private boolean _synthetic = false;

    /**
     * @deprecated
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        DBDictionary dict = vm.getMappingRepository().getDBDictionary();
        DBIdentifier colName = DBIdentifier.newColumn(name, dict != null ? dict.delimitAll() : false);
        return map(vm, colName, io, adapt);
    }
    
    public Column[] map(ValueMapping vm, DBIdentifier name, ColumnIO io,
        boolean adapt) {
        LinkedList cols = new LinkedList();
        LinkedList args = new LinkedList();
        super.map(vm, name, io, adapt, cols, args);

        ValueMappingInfo vinfo = vm.getValueInfo();
        Column nullInd = vinfo.getNullIndicatorColumn(vm, name,
            vm.getFieldMapping().getTable(), adapt);
        if (nullInd != null)
            vm.setColumns(new Column[]{ nullInd });

        // record index of null indicator column and whether it is synthetic
        if (nullInd != null) {
            _nullIdx = cols.indexOf(nullInd);
            if (_nullIdx == -1) {
                cols.addFirst(nullInd);
                args.addFirst(null);
                _nullIdx = 0;
                _synthetic = true;
            }
        }

        _vm = vm;
        _cols = (Column[]) cols.toArray(new Column[cols.size()]);
        _args = args.toArray();
        return _cols;
    }

    public boolean objectValueRequiresLoad(ValueMapping vm) {
        return true;
    }

    public Object getResultArgument(ValueMapping vm) {
        return _args;
    }

    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        OpenJPAStateManager em = store.getContext().getStateManager(val);
        Object rval = null;
        if (_cols.length > 1)
            rval = new Object[_cols.length];

        // set null indicator column
        int idx = 0;
        if (_synthetic) {
            Object cval = ((EmbeddedClassStrategy) vm.getEmbeddedMapping().
                getStrategy()).getNullIndicatorValue(em);
            if (_cols.length == 1)
                return cval;
            ((Object[]) rval)[idx++] = cval;
        }

        return super.toDataStoreValue(em, vm, store, _cols, rval, idx);
    }

    public Object toObjectValue(ValueMapping vm, Object val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        // check null indicator first
        if (_nullIdx != -1) {
            Object nval;
            if (_cols.length == 1)
                nval = val;
            else
                nval = ((Object[]) val)[_nullIdx];
            if (((EmbeddedClassStrategy) vm.getEmbeddedMapping().
                getStrategy()).indicatesNull(nval))
                return null;
        }

        // create embedded instance
        OpenJPAStateManager em = store.getContext().embed(null, null, sm, vm);
        int idx = (_synthetic) ? 1 : 0;
        super.toObjectValue(em, vm, val, store, fetch, _cols, idx);

        // after loading everything from result, load the rest of the
        // configured fields
        em.load(fetch);
        return em.getManagedInstance();
    }

    /////////////////////////////
    // RelationId implementation
    /////////////////////////////

    public Object toRelationDataStoreValue(OpenJPAStateManager sm, Column col) {
        return toRelationDataStoreValue(sm, col, 0);
    }

    /**
     * Recursive helper.
     */
    private Object toRelationDataStoreValue(OpenJPAStateManager sm, Column col,
        int idx) {
        FieldMapping field = findField(col, idx);
        if (field == null)
            throw new InternalException();

        if (field.getHandler() instanceof RelationId)
            return ((RelationId) field.getStrategy()).
                toRelationDataStoreValue(sm, col);
        if (field.getStrategy() instanceof RelationId)
            return ((RelationId) field.getStrategy()).
                toRelationDataStoreValue(sm, col);
        return toRelationDataStoreValue(sm, col, field.getIndex() + 1);
    }

    /**
     * Find the first field mapping that uses the given column starting with
     * the given field index.
     */
    private FieldMapping findField(Column col, int idx) {
        FieldMapping[] fms = _vm.getEmbeddedMapping().getFieldMappings();
        Column[] cols;
        for (int i = idx; i < fms.length; i++) {
            if (fms[i].getManagement() != FieldMapping.MANAGE_PERSISTENT)
                continue;
            cols = ((Embeddable) fms[i]).getColumns();
            for (int j = 0; j < cols.length; j++)
                if (cols[j] == col)
                    return fms[i];
        }
        return null;
	}
}
