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
package org.apache.openjpa.jdbc.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.InternalException;

/**
 * Manages SQL rows during an insert/update/delete process.
 *
 * @author Abe White
 * @nojavadoc
 */
public class RowManagerImpl
    implements RowManager {

    private Map<Key, PrimaryRow> _inserts = null;
    private Map<Key, PrimaryRow> _updates = null;
    private Map<Key, PrimaryRow> _deletes = null;
    private Collection<SecondaryRow> _secondaryUpdates = null;
    private Collection<SecondaryRow> _secondaryDeletes = null;
    private Collection<Row> _allRowUpdates = null;
    private Collection<Row> _allRowDeletes = null;

    // we maintain a list of the order of all primary rows if the user
    // wants to be able to fetch them in order
    private final List<PrimaryRow> _primaryOrder;

    // track whether we're dealing with any auto-inc columns
    private boolean _auto = false;

    // cache the last key and primary row; when looping over
    // all the field mappings of a class each one will probably ask for the
    // same key, so avoid the key creation and row lookup when possible
    private Key _key = null;
    private PrimaryRow _row = null;

    /**
     * Constructor.
     *
     * @param order whether to keep track of the order in which rows are added
     */
    public RowManagerImpl(boolean order) {
        _primaryOrder = (order) ? new ArrayList<PrimaryRow>() : null;
    }

    /**
     * Whether any primary rows have auto-assign constraints.
     */
    public boolean hasAutoAssignConstraints() {
        return _auto;
    }

    /**
     * Return the ordered primary rows. Only available if ordering requested
     * on construction.
     */
    public List<PrimaryRow> getOrdered() {
        if(_primaryOrder == null ) { 
            return Collections.emptyList();
        }
        else { 
            return _primaryOrder;
        }
    }

    /**
     * Return all inserted primary rows.
     */
    public Collection<PrimaryRow> getInserts() {
        if(_inserts == null ) {
            return Collections.emptyList();
        }
        else {
            return _inserts.values();
        }
    }

    /**
     * Return all updated primary rows.
     */
    public Collection<PrimaryRow> getUpdates() {
        if(_updates == null ){ 
            return Collections.emptyList();
        }
        else { 
            return _updates.values();
        }
    }

    /**
     * Return all deleted primary rows.
     */
    public Collection<PrimaryRow> getDeletes() {
        if(_deletes == null) { 
            return Collections.emptyList();
        }
        else {
            return _deletes.values();
        }
    }

    /**
     * Return all inserted and updated secondary rows.
     */
    public Collection<SecondaryRow> getSecondaryUpdates() {
        if(_secondaryUpdates == null) { 
            return Collections.emptyList();
        }
        else { 
            return _secondaryUpdates;
        }
    }

    /**
     * Return all deleted secondary rows.
     */
    public Collection<SecondaryRow> getSecondaryDeletes() {
        if(_secondaryDeletes == null) { 
            return Collections.emptyList();
        }
        else { 
            return _secondaryDeletes;
        }
    }

    /**
     * Return any 'all row' updates.
     */
    public Collection<Row> getAllRowUpdates() {
        if(_allRowUpdates == null) { 
            return Collections.emptyList();
        }
        else { 
            return _allRowUpdates;
        }
    }

    /**
     * Return any 'all row' deletes.
     */
    public Collection<Row> getAllRowDeletes() {
        if(_allRowDeletes == null) { 
            return Collections.emptyList();
        }
        else { 
            return _allRowDeletes;
        }
        
    }

    public Row getSecondaryRow(Table table, int action) {
        return new SecondaryRow(table, action);
    }

    public void flushSecondaryRow(Row row)
        throws SQLException {
        if (!row.isValid())
            return;

        SecondaryRow srow = (SecondaryRow) row;
        if (srow.getAction() == Row.ACTION_DELETE) {
            if (_secondaryDeletes == null)
                _secondaryDeletes = new ArrayList<SecondaryRow>();
            _secondaryDeletes.add((SecondaryRow) srow.clone());
        } else {
            if (_secondaryUpdates == null)
                _secondaryUpdates = new ArrayList<SecondaryRow>();
            _secondaryUpdates.add((SecondaryRow) srow.clone());
        }
    }

    public Row getAllRows(Table table, int action) {
        return new RowImpl(table, action);
    }

    public void flushAllRows(Row row) {
        if (!row.isValid())
            return;

        switch (row.getAction()) {
            case Row.ACTION_UPDATE:
                if (_allRowUpdates == null)
                    _allRowUpdates = new ArrayList<Row>();
                _allRowUpdates.add(row);
                break;
            case Row.ACTION_DELETE:
                if (_allRowDeletes == null)
                    _allRowDeletes = new ArrayList<Row>();
                _allRowDeletes.add(row);
                break;
            default:
                throw new InternalException("action = " + row.getAction());
        }
    }

    public Row getRow(Table table, int action, OpenJPAStateManager sm,
        boolean create) {
        if (sm == null)
            return null;

        // check if request matches cached version
        if (_key != null && _key.table == table && _key.sm == sm
            && _row != null && _row.getAction() == action)
            return _row;

        Map<Key, PrimaryRow> map;
        if (action == Row.ACTION_DELETE) {
            if (_deletes == null && create)
                _deletes = new LinkedHashMap<Key, PrimaryRow>();
            map = _deletes;
        } else if (action == Row.ACTION_INSERT) {
            if (_inserts == null && create)
                _inserts = new LinkedHashMap<Key, PrimaryRow>();
            map = _inserts;
        } else {
            if (_updates == null && create)
                _updates = new LinkedHashMap<Key, PrimaryRow>();
            map = _updates;
        }
        if (map == null)
            return null;

        _key = new Key(table, sm);
        _row = map.get(_key);

        if (_row == null && create) {
            _row = new PrimaryRow(table, action, sm);
            map.put(_key, _row);
            if (_primaryOrder != null) {
                _row.setIndex(_primaryOrder.size());
                _primaryOrder.add(_row);
            }

            if (!_auto && action == Row.ACTION_INSERT)
                _auto = table.getAutoAssignedColumns().length > 0;
        }

        if (_row != null)
            _row.setFailedObject(sm.getManagedInstance());
        return _row;
    }

    /**
     * Key for hashing virtual rows.
     */
    private static class Key {

        public final Table table;
        public final OpenJPAStateManager sm;

        public Key(Table table, OpenJPAStateManager sm) {
            this.table = table;
            this.sm = sm;
        }

        public int hashCode() {
            return ((table == null) ? 0  : table.hashCode()) + ((sm == null) ? 0  : sm.hashCode()) % Integer.MAX_VALUE;
        }

        public boolean equals(Object other) {
            if (other == null)
                return false;
            if (other == this)
                return true;

            Key key = (Key) other;
            return table == key.table && sm == key.sm;
        }
    }
}
