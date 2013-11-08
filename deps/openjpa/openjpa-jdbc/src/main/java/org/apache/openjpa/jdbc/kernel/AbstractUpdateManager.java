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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.Strategy;
import org.apache.openjpa.jdbc.meta.Version;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.OptimisticException;

/**
 * Base update manager with common functionality.
 *
 * @author Abe White
 */
public abstract class AbstractUpdateManager
    implements UpdateManager, Configurable {

    protected JDBCConfiguration conf = null;
    protected DBDictionary dict = null;

    public void setConfiguration(Configuration conf) {
        this.conf = (JDBCConfiguration) conf;
        dict = this.conf.getDBDictionaryInstance();
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }

    public Collection flush(Collection states, JDBCStore store) {
        Connection conn = store.getConnection();
        try {
            PreparedStatementManager psMgr = newPreparedStatementManager(store,
                conn);
            return flush(states, store, psMgr);
        } finally {
            try { conn.close(); } catch (SQLException se) {}
        }
    }

    protected Collection flush(Collection states, JDBCStore store,
        PreparedStatementManager psMgr) {
        // run through all the states and update them as necessary
        RowManager rowMgr = newRowManager();
        Collection customs = new LinkedList();
        Collection exceps = psMgr.getExceptions();
        Collection mappedByIdStates = new ArrayList();
        for (Iterator itr = states.iterator(); itr.hasNext();) {
            OpenJPAStateManager obj = (OpenJPAStateManager)itr.next();
            if (obj instanceof StateManagerImpl) {
                StateManagerImpl sm = (StateManagerImpl) obj;
                if (sm.getMappedByIdFields() != null)
                    mappedByIdStates.add(sm);
                else exceps = populateRowManager(sm, rowMgr, store, exceps,
                        customs);
            } else 
                exceps = populateRowManager(obj, rowMgr, store, exceps,
                        customs);
        }

        // flush rows
        exceps = flush(rowMgr, psMgr, exceps);
        
        if (mappedByIdStates.size() != 0) {
            for (Iterator itr = mappedByIdStates.iterator(); itr.hasNext();) {
                StateManagerImpl sm = (StateManagerImpl) itr.next();
                exceps = populateRowManager(sm, rowMgr, store, exceps, customs);
            }
            // flush rows
            exceps = flush(rowMgr, psMgr, exceps);
        }

        // now do any custom mappings
        for (Iterator itr = customs.iterator(); itr.hasNext();) {
            try {
                ((CustomMapping) itr.next()).execute(store);
            } catch (SQLException se) {
                exceps = addException(exceps, SQLExceptions.getStore(se, dict));
            } catch (OpenJPAException ke) {
                exceps = addException(exceps, ke);
            }
        }

        // return all exceptions
        return exceps;
    }

    /**
     * Return a new {@link RowManager}.
     */
    protected abstract RowManager newRowManager();

    /**
     * Return a new {@link PreparedStatementManager}.
     */
    protected abstract PreparedStatementManager newPreparedStatementManager(
        JDBCStore store, Connection conn);

    /**
     * Flush all rows of the given row manager. Add exceptions to
     * <code>exceps</code> (which may start as null) using
     * {@link #addException}. Return <code>exceps</code>.
     */
    protected abstract Collection flush(RowManager rowMgr,
        PreparedStatementManager psMgr, Collection exceps);

    /**
     * Populate the row manager with rows to be flushed for the given state.
     *
     * @param exceps exceptions encountered when flushing will be added to
     * this list and returned; the list may be null initially
     * @param customs buffer custom mappings
     * @return the exceptions list
     */
    protected Collection populateRowManager(OpenJPAStateManager sm,
        RowManager rowMgr, JDBCStore store, Collection exceps,
        Collection customs) {
    	int action = Row.ACTION_UPDATE;
        try {
            BitSet dirty;
            if (sm.getPCState() == PCState.PNEW && !sm.isFlushed()) {
            	action = Row.ACTION_INSERT;
            	insert(sm, (ClassMapping) sm.getMetaData(), rowMgr, store,
                    customs);
            } else if (sm.getPCState() == PCState.PNEWFLUSHEDDELETED
                || sm.getPCState() == PCState.PDELETED) {
            	action = Row.ACTION_DELETE;
            	delete(sm, (ClassMapping) sm.getMetaData(), rowMgr, store,
                    customs);
            } else if ((dirty = ImplHelper.getUpdateFields(sm)) != null) {
                update(sm, dirty, (ClassMapping) sm.getMetaData(), rowMgr,
                    store, customs, false);
            } else if (sm.isVersionUpdateRequired()) {
                updateIndicators(sm, (ClassMapping) sm.getMetaData(), rowMgr,
                    store, customs, true);
            } else if (sm.isVersionCheckRequired()) {
                if (!((ClassMapping) sm.getMetaData()).getVersion().
                    checkVersion(sm, store, false))
                    exceps = addException(exceps, new OptimisticException(sm.
                        getManagedInstance()));
            }
        } catch (SQLException se) {
            exceps = addException(exceps, SQLExceptions.getStore(se, dict));
        } catch (OpenJPAException ke) {
        	RowImpl row = (RowImpl) rowMgr.getRow(((ClassMapping) sm.getMetaData()).getTable(), action, sm, false);
            if (row != null) {
                row.setFlushed(true);
            } 
            exceps = addException(exceps, ke);
        }
        return exceps;
    }

    /**
     * Add the given exception to the given list, which may start out as null.
     */
    protected Collection addException(Collection exceps, Exception err) {
        if (exceps == null)
            exceps = new LinkedList();
        exceps.add(err);
        return exceps;
    }

    /**
     * Recursive method to insert the given instance, base class first.
     */
    protected void insert(OpenJPAStateManager sm, ClassMapping mapping,
        RowManager rowMgr, JDBCStore store, Collection customs)
        throws SQLException {
        Boolean custom = mapping.isCustomInsert(sm, store);
        if (!Boolean.FALSE.equals(custom))
            mapping.customInsert(sm, store);
        if (Boolean.TRUE.equals(custom))
            return;

        ClassMapping sup = mapping.getJoinablePCSuperclassMapping();
        if (sup != null)
            insert(sm, sup, rowMgr, store, customs);

        mapping.insert(sm, store, rowMgr);
        FieldMapping[] fields = mapping.getDefinedFieldMappings();
        if (((StateManagerImpl)sm).getMappedByIdFields() != null) {
            // when there is mappedByIdFields, the id field is not
            // fully populated. We need to insert other fields first
            // so that in the process of inserting other fields,
            // the values of mappedById fields can be set into
            // the id fields. Once the id fields are fully populated,
            // we will then insert the id fields.
            fields = reorderFields(fields);
        }  
        
        BitSet dirty = sm.getDirty();
        for (int i = 0; i < fields.length; i++) {
            if (dirty.get(fields[i].getIndex())
                && !bufferCustomInsert(fields[i], sm, store, customs)) {
                fields[i].insert(sm, store, rowMgr);
            }
        }
        if (sup == null) {
            Version vers = mapping.getVersion();
            if (!bufferCustomInsert(vers, sm, store, customs))
                vers.insert(sm, store, rowMgr);
            Discriminator dsc = mapping.getDiscriminator();
            if (!bufferCustomInsert(dsc, sm, store, customs))
                dsc.insert(sm, store, rowMgr);
        }
    }
    
    private FieldMapping[] reorderFields(FieldMapping[] fields) {
        List<FieldMapping> pkFmds = new ArrayList<FieldMapping>();
        FieldMapping[] ret = new FieldMapping[fields.length];
        int j = 0;
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].isPrimaryKey())
                ret[j++] = fields[i];
            else
                pkFmds.add(fields[i]);
        }
        for (int i = 0; i <pkFmds.size(); i++) {
            ret[j++] = pkFmds.get(i);
        }
        return ret;
    }

    /**
     * If the given mapping uses a custom insert, places a
     * {@link CustomMapping} struct for it in the given collection and
     * returns true, else returns false.
     */
    private boolean bufferCustomInsert(Strategy strat, OpenJPAStateManager sm,
        JDBCStore store, Collection customs) {
        Boolean custom = strat.isCustomInsert(sm, store);
        if (!Boolean.FALSE.equals(custom))
            customs.add(new CustomMapping(CustomMapping.INSERT, sm, strat));
        return Boolean.TRUE.equals(custom);
    }

    /**
     * Recursive method to delete the given instance, base class last.
     */
    protected void delete(OpenJPAStateManager sm, ClassMapping mapping,
        RowManager rowMgr, JDBCStore store, Collection customs)
        throws SQLException {
        Boolean custom = mapping.isCustomDelete(sm, store);
        if (!Boolean.FALSE.equals(custom))
            mapping.customDelete(sm, store);
        if (Boolean.TRUE.equals(custom))
            return;

        FieldMapping[] fields = mapping.getDefinedFieldMappings();
        for (int i = 0; i < fields.length; i++)
            if (!bufferCustomDelete(fields[i], sm, store, customs))
                fields[i].delete(sm, store, rowMgr);

        ClassMapping sup = mapping.getJoinablePCSuperclassMapping();
        if (sup == null) {
            Version vers = mapping.getVersion();
            if (!bufferCustomDelete(vers, sm, store, customs))
                vers.delete(sm, store, rowMgr);
            Discriminator dsc = mapping.getDiscriminator();
            if (!bufferCustomDelete(dsc, sm, store, customs))
                dsc.delete(sm, store, rowMgr);
        }
        mapping.delete(sm, store, rowMgr);

        if (sup != null)
            delete(sm, sup, rowMgr, store, customs);
    }

    /**
     * @see #bufferCustomInsert
     */
    private boolean bufferCustomDelete(Strategy strat, OpenJPAStateManager sm,
        JDBCStore store, Collection customs) {
        Boolean custom = strat.isCustomDelete(sm, store);
        if (!Boolean.FALSE.equals(custom))
            customs.add(new CustomMapping(CustomMapping.DELETE, sm, strat));
        return Boolean.TRUE.equals(custom);
    }

    /**
     * Recursive method to update the given instance.
     */
    protected void update(OpenJPAStateManager sm, BitSet dirty,
        ClassMapping mapping, RowManager rowMgr, JDBCStore store,
        Collection customs, boolean updateIndicators) throws SQLException {
        Boolean custom = mapping.isCustomUpdate(sm, store);
        if (!Boolean.FALSE.equals(custom))
            mapping.customUpdate(sm, store);
        if (Boolean.TRUE.equals(custom))
            return;

        // update all fields before all mappings so that the mappings can
        // detect whether any fields in their rows have been modified
        FieldMapping[] fields = mapping.getDefinedFieldMappings();
        for (int i = 0; i < fields.length; i++) {
            FieldMapping field = fields[i];
            if (dirty.get(field.getIndex())
                && !bufferCustomUpdate(field, sm, store, customs)) {
                field.update(sm, store, rowMgr);
                if (!updateIndicators) {
                    FieldMapping[] inverseFieldMappings =
                        field.getInverseMappings();
                    if (inverseFieldMappings.length == 0) {
                        updateIndicators = true;
                    }
                    else {
                        for (FieldMapping inverseFieldMapping :
                            inverseFieldMappings) {
                            if (inverseFieldMapping.getMappedBy() != null) {
                                updateIndicators = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        ClassMapping sup = mapping.getJoinablePCSuperclassMapping();
        if (sup == null) {
            if (updateIndicators) {
                updateIndicators(sm, mapping, rowMgr, store, customs, false);
            }
        }
        else
            update(sm, dirty, sup, rowMgr, store, customs, updateIndicators);

        mapping.update(sm, store, rowMgr);
    }

    /**
     * Update version and discriminator indicators.
     */
    protected void updateIndicators(OpenJPAStateManager sm,
            ClassMapping mapping, RowManager rowMgr, JDBCStore store,
            Collection customs,
        boolean versionUpdateOnly) throws SQLException {
        while (mapping.getJoinablePCSuperclassMapping() != null)
            mapping = mapping.getJoinablePCSuperclassMapping();

        Version vers = mapping.getVersion();
        if (!bufferCustomUpdate(vers, sm, store, customs))
            vers.update(sm, store, rowMgr);

        if (versionUpdateOnly) {
            // if we are only updating the version column, we need to add
            // in the primary key select
            mapping.update(sm, store, rowMgr);
        } else {
            // otherwise we need to make sure we update the discriminator too
            Discriminator dsc = mapping.getDiscriminator();
            if (!bufferCustomUpdate(dsc, sm, store, customs))
                dsc.update(sm, store, rowMgr);
        }
    }

    /**
     * @see #bufferCustomInsert
     */
    private boolean bufferCustomUpdate(Strategy strat, OpenJPAStateManager sm,
        JDBCStore store, Collection customs) {
        Boolean custom = strat.isCustomUpdate(sm, store);
        if (!Boolean.FALSE.equals(custom))
            customs.add(new CustomMapping(CustomMapping.UPDATE, sm, strat));
        return Boolean.TRUE.equals(custom);
    }

    /**
     * Executes customized mapping updates.
     */
    protected static class CustomMapping {

        public static final int INSERT = 0;
        public static final int UPDATE = 1;
        public static final int DELETE = 3;

        private final int _action;
        private final OpenJPAStateManager _sm;
        private final Strategy _strat;

        public CustomMapping(int action, OpenJPAStateManager sm, Strategy strat)
        {
            _action = action;
            _sm = sm;
            _strat = strat;
        }

        public void execute(JDBCStore store) throws SQLException {
            switch (_action) {
            case INSERT:
                _strat.customInsert(_sm, store);
                break;
            case UPDATE:
                _strat.customUpdate(_sm, store);
                break;
            case DELETE:
                _strat.customDelete(_sm, store);
                break;
            }
        }
    }
}
