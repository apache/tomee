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
import java.util.BitSet;
import java.util.Collection;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.ArrayStateImage;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Uses a state image to determine whether concurrency violations take place.
 *
 * @author Abe White
 */
public class StateComparisonVersionStrategy
    extends AbstractVersionStrategy {

    public static final String ALIAS = "state-comparison";

    private static final Localizer _loc = Localizer.forPackage
        (StateComparisonVersionStrategy.class);

    public String getAlias() {
        return ALIAS;
    }

    public void map(boolean adapt) {
        ClassMapping cls = vers.getClassMapping();
        if (cls.getJoinablePCSuperclassMapping() != null
            || cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-base-vers", cls));

        vers.getMappingInfo().assertNoSchemaComponents(vers, true);
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        FieldMapping[] fields = (FieldMapping[]) sm.getMetaData().getFields();
        Object[] state = ArrayStateImage.newImage(fields.length);
        BitSet loaded = ArrayStateImage.getLoaded(state);

        // take a snapshot of all versionable field values
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].isPrimaryKey() && fields[i].isVersionable()) {
                loaded.set(i);
                state[i] = sm.fetch(fields[i].getIndex());
            }
        }
        sm.setNextVersion(state);
    }

    /**
     * This method is for class mappings that take over the insert
     * process, but still want to use this indicator for optimistic locking.
     */
    public void customInsert(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        insert(sm, store, null);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        // if there is no recorded state (for example, modification made to
        // hollow instance and no fields every loaded), can't do anything
        Object[] state = (Object[]) sm.getVersion();
        if (state == null)
            return;

        BitSet loaded = ArrayStateImage.getLoaded(state);
        Object[] nextState = ArrayStateImage.clone(state);

        // loop through fields and update changing values for the next state
        // image, plus add WHERE conditions on updates to make sure that
        // db values match our previous image
        FieldMapping[] fields = (FieldMapping[]) sm.getMetaData().getFields();
        Row row;
        if (sm.isVersionCheckRequired()) {
            for (int i = 0, max = loaded.length(); i < max; i++) {
                if (!loaded.get(i))
                    continue;

                // update our next state image with the new field value
                if (sm.getDirty().get(i) && !sm.getFlushed().get(i))
                    nextState[i] = sm.fetch(fields[i].getIndex());

                // fetch the row for this field; if no row exists, then we can't
                // add one because we have no updates to perform; that means we
                // won't detect OL exceptions when another transaction changes
                // fields that aren't in any of the same tables as fields that
                // this transaction changed
                row = rm.getRow(fields[i].getTable(), Row.ACTION_UPDATE,
                    sm, false);
                if (row == null)
                    continue;

                // set WHERE criteria matching the previous state image so the
                // update will fail for any changes made by another transaction
                fields[i].where(sm, store, rm, state[i]);
                row.setFailedObject(sm.getManagedInstance());
            }
        }
        sm.setNextVersion(nextState);
    }

    /**
     * This method is for class mappings that take over the update
     * process, but still want to use this indicator for optimistic locking.
     *
     * @param sm the instance to test
     * @param store store manager context
     * @param table only state image values in this table will be tested;
     * if the custom mapping uses different updates for
     * different tables, this method can be called multiple
     * times for the multiple tables
     * @param record set this parameter to true the last time you call
     * this method, so the indicator can setup the next
     * version of the given state manager
     * @return a {@link CustomUpdate} whose getSQL method yields a
     * boolean SQL expression that tests whether the current
     * record is equal to our recorded state image, and whose
     * setParameters method parameterizes the given prepared
     * statement with the values used in the above boolean expression
     */
    public CustomUpdate customUpdate(OpenJPAStateManager sm, JDBCStore store,
        Table table, boolean record)
        throws SQLException {
        CustomUpdate custom = new CustomUpdate(table);
        Object[] state = (Object[]) sm.getVersion();
        if (state == null)
            return custom;

        BitSet loaded = ArrayStateImage.getLoaded(state);
        Object[] nextState = null;
        if (record)
            nextState = ArrayStateImage.clone(state);

        FieldMapping[] fields = (FieldMapping[]) sm.getMetaData().getFields();
        for (int i = 0, max = loaded.length(); i < max; i++) {
            if (!loaded.get(i))
                continue;

            if (record && sm.getDirty().get(i) && !sm.getFlushed().get(i))
                nextState[i] = sm.fetch(fields[i].getIndex());
            if (fields[i].getTable() == table)
                fields[i].where(sm, store, custom, state[i]);
        }
        if (record)
            sm.setNextVersion(nextState);

        return custom;
    }

    public void afterLoad(OpenJPAStateManager sm, JDBCStore store) {
        FieldMapping[] fields = (FieldMapping[]) sm.getMetaData().getFields();

        Object[] state = (Object[]) sm.getVersion();
        if (state == null)
            state = ArrayStateImage.newImage(fields.length);
        BitSet loaded = ArrayStateImage.getLoaded(state);

        // take a snapshot of all versionable field values that were loaded
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].isPrimaryKey()
                && fields[i].isVersionable()
                && sm.getLoaded().get(fields[i].getIndex())
                && !loaded.get(i)
                && !sm.getDirty().get(fields[i].getIndex())) {
                loaded.set(i);
                state[i] = sm.fetch(fields[i].getIndex());
            }
        }
        sm.setVersion(state);
    }

    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException {
        if (updateVersion)
            sm.setVersion(null);
        return !updateVersion;
    }

    public int compareVersion(Object v1, Object v2) {
        return (ArrayStateImage.sameVersion((Object[]) v1, (Object[]) v2))
            ? StoreManager.VERSION_SAME : StoreManager.VERSION_DIFFERENT;
    }

    /**
     * Row implementation we use to pass to versionable mappings so they
     * can set up the where conditions we need to add to update statements.
     *
     * @author Abe White
     */
    public static class CustomUpdate
        extends RowImpl
        implements RowManager {

        private CustomUpdate(Table table) {
            this(table.getColumns());
        }

        private CustomUpdate(Column[] cols) {
            super(cols, Row.ACTION_UPDATE);
        }

        /**
         * Return a boolean SQL expression that should be added to the
         * WHERE clause of an UPDATE to test whether the current database
         * record matches our stored version.
         */
        public String getSQL(DBDictionary dict) {
            Column[] cols = getTable().getColumns();
            StringBuilder buf = new StringBuilder();
            boolean hasWhere = false;
            Object val;
            for (int i = 0; i < cols.length; i++) {
                val = getWhere(cols[i]);
                if (val == null)
                    continue;

                if (hasWhere)
                    buf.append(" AND ");
                if (val == NULL)
                    buf.append(dict.getColumnDBName(cols[i]) + " IS NULL");
                else
                    buf.append(dict.getColumnDBName(cols[i]) + " = ?");
                hasWhere = true;
            }
            return buf.toString();
        }

        protected RowImpl newInstance(Column[] cols, int action) {
            return new CustomUpdate(cols);
        }

        /////////////////////////////
        // RowManager implementation
        /////////////////////////////

        public boolean hasAutoAssignConstraints() {
            return false;
        }

        public Collection getInserts() {
            throw new InternalException();
        }

        public Collection getUpdates() {
            throw new InternalException();
        }

        public Collection getDeletes() {
            throw new InternalException();
        }

        public Collection getSecondaryUpdates() {
            throw new InternalException();
        }

        public Collection getSecondaryDeletes() {
            throw new InternalException();
        }

        public Collection getAllRowUpdates() {
            throw new InternalException();
        }

        public Collection getAllRowDeletes() {
            throw new InternalException();
        }

        public Row getRow(Table table, int action, OpenJPAStateManager sm,
            boolean create) {
            // verionable mappings will never want to create rows, so we
            // can always safely return null
            if (table != getTable())
                return null;
            return this;
        }

        public Row getSecondaryRow(Table table, int action) {
            throw new InternalException();
        }

        public void flushSecondaryRow(Row row) {
        }

        public Row getAllRows(Table table, int action) {
            throw new InternalException();
        }

        public void flushAllRows(Row row) {
        }

        public void setObject(Column col, Object val)
            throws SQLException {
            throw new InternalException();
        }
    }
}
