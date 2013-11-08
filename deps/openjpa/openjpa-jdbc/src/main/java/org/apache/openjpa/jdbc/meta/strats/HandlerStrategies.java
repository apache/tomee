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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.meta.ValueHandler;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Utility methods for strategies using value handlers.
 *
 * @author Abe White
 * @since 0.4.0
 */
public class HandlerStrategies {

    private static final Localizer _loc = Localizer.forPackage
        (HandlerStrategies.class);

    /**
     * Map the given value.
     */
    public static Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        ValueMappingInfo vinfo = vm.getValueInfo();
        vinfo.assertNoJoin(vm, true);
        vinfo.assertNoForeignKey(vm, !adapt);

        DBDictionary dict = vm.getMappingRepository().getDBDictionary();
        DBIdentifier colName = DBIdentifier.newColumn(name, dict != null ? dict.delimitAll() : false);
        Column[] cols = vm.getHandler().map(vm, colName.getName(), io, adapt);
        if (cols.length > 0 && cols[0].getTable() == null) {
            cols = vinfo.getColumns(vm, colName, cols,
                vm.getFieldMapping().getTable(), adapt);
            if (vinfo.isImplicitRelation())
            	for (int i = 0; i < cols.length; i++)
            		cols[i].setImplicitRelation(true);
            ColumnIO mappedIO = vinfo.getColumnIO();
            vm.setColumns(cols);
            vm.setColumnIO(mappedIO);
            if (mappedIO != null) {
                for (int i = 0; i < cols.length; i++) {
                    io.setInsertable(i, mappedIO.isInsertable(i, false));
                    io.setNullInsertable(i, mappedIO.isInsertable(i, true));
                    io.setUpdatable(i, mappedIO.isUpdatable(i, false));
                    io.setNullUpdatable(i, mappedIO.isUpdatable(i, true));
                }
            }
        }
        vm.mapConstraints(colName, adapt);
        return cols;
    }

    /**
     * Set the given value into the given row.
     * Return false if the given value can not be set, for example, due to 
     * null constraints on the columns. 
     */
    public static boolean set(ValueMapping vm, Object val, JDBCStore store,
        Row row, Column[] cols, ColumnIO io, boolean nullNone)
        throws SQLException {
        if (!canSetAny(row, io, cols))
            return false;

        ValueHandler handler = vm.getHandler();
        val = handler.toDataStoreValue(vm, val, store);
        boolean isSet = false;
        if (val == null) {
            for (int i = 0; i < cols.length; i++)
                if (canSet(row, io, i, true)) {
                    isSet = true;
                    set(row, cols[i], null, handler, nullNone);
                }
        } else if (cols.length == 1) {
            if (canSet(row, io, 0, val == null)) {
                isSet = true;
                set(row, cols[0], val, handler, nullNone);
            }
        } else {
            Object[] vals = (Object[]) val;
            for (int i = 0; i < vals.length; i++)
                if (canSet(row, io, i, vals[i] == null)) {
                    isSet = true;
                    set(row, cols[i], vals[i], handler, nullNone);
                }
        }
        return isSet;
    }

    /**
     * Return true if the given column index is settable.
     */
    private static boolean canSet(Row row, ColumnIO io, int i,
        boolean nullValue) {
        if (row.getAction() == Row.ACTION_INSERT)
        	return io.isInsertable(i, nullValue);
        if (row.getAction() == Row.ACTION_UPDATE)
        	return io.isUpdatable(i, nullValue);
        return true;
    }

    /**
     * Return true if the any column up to the given index is settable.
     */
    private static boolean canSetAny(Row row, ColumnIO io, Column[] cols) {
        if (row.getAction() == Row.ACTION_INSERT)
            return io.isAnyInsertable(cols, false);
        if (row.getAction() == Row.ACTION_UPDATE)
            return io.isAnyUpdatable(cols, false);
        return true;
    }

    /**
     * Set a value into a row, taking care not to override column defaults
     * with nulls unless the user wants us to.
     */
    private static void set(Row row, Column col, Object val,
        ValueHandler handler, boolean nullNone)
        throws SQLException {
        if (val == null)
            row.setNull(col, nullNone);
        else if (col.isRelationId() && handler instanceof RelationId)
            row.setRelationId(col, (OpenJPAStateManager) val,
                (RelationId) handler);
        else
            row.setObject(col, val);
    }

    /**
     * Add where conditions to the given row.
     */
    public static void where(ValueMapping vm, Object val, JDBCStore store,
        Row row, Column[] cols)
        throws SQLException {
        if (cols.length == 0)
            return;

        val = toDataStoreValue(vm, val, cols, store);
        if (val == null)
            for (int i = 0; i < cols.length; i++)
                row.whereNull(cols[i]);
        else if (cols.length == 1)
            where(row, cols[0], val);
        else {
            Object[] vals = (Object[]) val;
            for (int i = 0; i < vals.length; i++)
                where(row, cols[i], vals[i]);
        }
    }

    /**
     * Set a where condition on the given row.
     */
    private static void where(Row row, Column col, Object val)
        throws SQLException {
        if (val == null)
            row.whereNull(col);
        else
            row.whereObject(col, val);
    }

    /**
     * Load the Object value from the given result.
     */
    public static Object loadObject(ValueMapping vm, OpenJPAStateManager sm,
        JDBCStore store, JDBCFetchConfiguration fetch, Result res,
        Joins joins, Column[] cols, boolean objectValueRequiresLoad)
        throws SQLException {
        if (cols.length == 0)
            throw new InvalidStateException(_loc.get("cant-project-owned",
                vm));

        Object val = loadDataStore(vm, res, joins, cols);
        if (objectValueRequiresLoad)
            return vm.getHandler().toObjectValue(vm, val, sm, store, fetch);
        return vm.getHandler().toObjectValue(vm, val);
    }

    /**
     * Load the datastore value from the given result. This method does
     * <b>not</b> process the loaded value through
     * {@link ValueHandler#toObjectValue}.
     */
    public static Object loadDataStore(ValueMapping vm, Result res,
        Joins joins, Column[] cols)
        throws SQLException {
        if (cols.length == 0)
            return null;
        if (cols.length == 1)
            return res.getObject(cols[0], vm.getHandler().
                getResultArgument(vm), joins);

        Object[] vals = new Object[cols.length];
        Object[] args = (Object[]) vm.getHandler().getResultArgument(vm);
        for (int i = 0; i < cols.length; i++)
            vals[i] = res.getObject(cols[i], (args == null) ? null : args[i],
                joins);
        return vals;
    }

    /**
     * Convert the given object to its datastore value(s). Relation ids are
     * converted to their final values immediately.
     */
    public static Object toDataStoreValue(ValueMapping vm, Object val,
        Column[] cols, JDBCStore store) {
        ValueHandler handler = vm.getHandler();
        val = handler.toDataStoreValue(vm, val, store);
        if (val == null) {
            if (cols.length > 1)
                return new Object[cols.length];
            return null;
        }

        // relation ids are returned as state managers; resolve the final
        // datastore value immediately
        Object[] vals;
        for (int i = 0; i < cols.length; i++) {
            if (!cols[i].isRelationId())
                continue;
            if (!(handler instanceof RelationId))
                break;
            if (cols.length == 1) {
                val = ((RelationId) handler).toRelationDataStoreValue
                    ((OpenJPAStateManager) val, cols[i]);
            } else {
                vals = (Object[]) val;
                vals[i] = ((RelationId) handler).toRelationDataStoreValue
                    ((OpenJPAStateManager) vals[i], cols[i]);
            }
        }
        return val;
    }

    /**
     * Throw the proper exception if the given handler-controlled value
     * represents an unjoinable relation.
     */
    public static void assertJoinable(ValueMapping vm) {
        ClassMapping rel = vm.getTypeMapping();
        if (rel != null && (rel.getTable() == null
            || !rel.getTable().equals(vm.getFieldMapping().getTable())))
            throw RelationStrategies.unjoinable(vm);
    }
}
