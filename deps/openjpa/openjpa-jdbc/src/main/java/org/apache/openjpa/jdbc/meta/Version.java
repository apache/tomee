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
package org.apache.openjpa.jdbc.meta;

import java.sql.SQLException;
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.MetaDataContext;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.InternalException;

/**
 * Handles optimistic lock versioning for a class.
 *
 * @author Abe White
 */
public class Version
    implements VersionStrategy, MetaDataContext, MetaDataModes {

    private static final Localizer _loc = Localizer.forPackage(Version.class);

    private final ClassMapping _mapping;
    private final VersionMappingInfo _info;
    private VersionStrategy _strategy = null;
    private int _resMode = MODE_NONE;

    private Column[] _cols = Schemas.EMPTY_COLUMNS;
    private ColumnIO _io = null;
    private Index _idx = null;

    /**
     * Constructor. Supply owning mapping.
     */
    public Version(ClassMapping mapping) {
        _mapping = mapping;
        _info = getMappingRepository().newMappingInfo(this);
    }

    public MetaDataRepository getRepository() {
        return _mapping.getRepository();
    }

    public MappingRepository getMappingRepository() {
        return _mapping.getMappingRepository();
    }

    /**
     * Return the owning mapping.
     */
    public ClassMapping getClassMapping() {
        return _mapping;
    }

    /**
     * The strategy used for versioning.
     */
    public VersionStrategy getStrategy() {
        return _strategy;
    }

    /**
     * The strategy used for versioning. The <code>adapt</code>
     * parameter determines whether to adapt when mapping the strategy;
     * use null if the strategy should not be mapped.
     */
    public void setStrategy(VersionStrategy strategy, Boolean adapt) {
        // set strategy first so we can access it during mapping
        VersionStrategy orig = _strategy;
        _strategy = strategy;
        if (strategy != null) {
            try {
                strategy.setVersion(this);
                if (adapt != null)
                    strategy.map(adapt.booleanValue());
            } catch (RuntimeException re) {
                // reset strategy
                _strategy = orig;
                throw re;
            }
        }
    }

    /**
     * Raw mapping data.
     */
    public VersionMappingInfo getMappingInfo() {
        return _info;
    }

    /**
     * Columns used for versioning.
     */
    public Column[] getColumns() {
        return _cols;
    }

    /**
     * Columns used for versioning.
     */
    public void setColumns(Column[] cols) {
        if (cols == null)
            cols = Schemas.EMPTY_COLUMNS;
        _cols = cols;
    }

    /**
     * I/O information on the version columns.
     */
    public ColumnIO getColumnIO() {
        return (_io == null) ? ColumnIO.UNRESTRICTED : _io;
    }

    /**
     * I/O information on the version columns.
     */
    public void setColumnIO(ColumnIO io) {
        _io = io;
    }

    /**
     * Index on the version columns, or null if none.
     */
    public Index getIndex() {
        return _idx;
    }

    /**
     * Index on the version columns, or null if none.
     */
    public void setIndex(Index idx) {
        _idx = idx;
    }

    /**
     * Increment the reference count of used schema components.
     */
    public void refSchemaComponents() {
        for (int i = 0; i < _cols.length; i++)
            _cols[i].ref();
    }

    /**
     * Clear mapping information, including strategy.
     */
    public void clearMapping() {
        _strategy = null;
        _cols = Schemas.EMPTY_COLUMNS;
        _idx = null;
        _info.clear();
        setResolve(MODE_MAPPING | MODE_MAPPING_INIT, false);
    }

    /**
     * Update {@link MappingInfo} with our current mapping information.
     */
    public void syncMappingInfo() {
        _info.syncWith(this);

        // setup the version field with our mapping info
        FieldMapping fm = _mapping.getVersionFieldMapping();
        if (fm != null) {
            // erase explicit strategy if it is standard version strategy for
            // field type
            if (_info.getStrategy() != null
                && _info.getStrategy().equals(getMappingRepository().
                defaultStrategy(this, fm).getAlias()))
                _info.setStrategy(null);

            fm.getMappingInfo().clear();
            fm.getValueInfo().clear();
            fm.getKeyMapping().getValueInfo().clear();
            fm.getElementMapping().getValueInfo().clear();
            fm.getValueInfo().copy(_info);
            _info.clear();
        }
    }

    /**
     * Resolve mode.
     */
    public int getResolve() {
        return _resMode;
    }

    /**
     * Resolve mode.
     */
    public void setResolve(int mode) {
        _resMode = mode;
    }

    /**
     * Resolve mode.
     */
    public void setResolve(int mode, boolean on) {
        if (mode == MODE_NONE)
            _resMode = mode;
        else if (on)
            _resMode |= mode;
        else
            _resMode &= ~mode;
    }

    /**
     * Resolve mapping information.
     */
    public boolean resolve(int mode) {
        if ((_resMode & mode) == mode)
            return true;
        int cur = _resMode;
        _resMode |= mode;
        if ((mode & MODE_MAPPING) != 0 && (cur & MODE_MAPPING) == 0)
            resolveMapping();
        if ((mode & MODE_MAPPING_INIT) != 0 && (cur & MODE_MAPPING_INIT) == 0)
            _strategy.initialize();
        return false;
    }

    /**
     * Setup mapping.
     */
    private void resolveMapping() {
        // if there is a version field, copy mapping info from it
        FieldMapping fm = _mapping.getVersionFieldMapping();
        if (fm != null)
            _info.copy(fm.getValueInfo());

        // map strategy
        MappingRepository repos = getMappingRepository();
        if (_strategy == null)
            repos.getStrategyInstaller().installStrategy(this);
        Log log = repos.getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("strategy", this, _strategy.getAlias()));

        // mark columns as mapped
        Column[] cols = getColumns();
        ColumnIO io = getColumnIO();
        for (int i = 0; i < cols.length; i++) {
            if (io.isInsertable(i, false))
                cols[i].setFlag(Column.FLAG_DIRECT_INSERT, true);
            if (io.isUpdatable(i, false))
                cols[i].setFlag(Column.FLAG_DIRECT_UPDATE, true);
        }
    }

    //////////////////////////////////
    // VersionStrategy implementation
    //////////////////////////////////

    public String getAlias() {
        return assertStrategy().getAlias();
    }

    public void map(boolean adapt) {
        assertStrategy().map(adapt);
    }

    public void initialize() {
        assertStrategy().initialize();
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().insert(sm, store, rm);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().update(sm, store, rm);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        assertStrategy().delete(sm, store, rm);
    }

    public Boolean isCustomInsert(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomInsert(sm, store);
    }

    public Boolean isCustomUpdate(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomUpdate(sm, store);
    }

    public Boolean isCustomDelete(OpenJPAStateManager sm, JDBCStore store) {
        return assertStrategy().isCustomDelete(sm, store);
    }

    public void customInsert(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customInsert(sm, store);
    }

    public void customUpdate(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customUpdate(sm, store);
    }

    public void customDelete(OpenJPAStateManager sm, JDBCStore store)
        throws SQLException {
        assertStrategy().customDelete(sm, store);
    }

    public void setVersion(Version owner) {
        assertStrategy().setVersion(owner);
    }

    public boolean select(Select sel, ClassMapping mapping) {
        return assertStrategy().select(sel, mapping);
    }

    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res)
        throws SQLException {
        return assertStrategy().load(sm, store, res);
    }

    public Object load(OpenJPAStateManager sm, JDBCStore store, Result res, Joins joins)
        throws SQLException {
        return assertStrategy().load(sm, store, res, joins);
    }
    
    public void afterLoad(OpenJPAStateManager sm, JDBCStore store) {
        assertStrategy().afterLoad(sm, store);
    }

    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException {
        return assertStrategy().checkVersion(sm, store, updateVersion);
    }

    public int compareVersion(Object v1, Object v2) {
        return assertStrategy().compareVersion(v1, v2);
    }

    private VersionStrategy assertStrategy() {
        if (_strategy == null)
            throw new InternalException();
        return _strategy;
    }

    public String toString() {
        return _mapping + "<version>";
    }

    /**
     * @return a Map<Column,Object> specifying how to update each version
     * column in this instance during a bulk update.
     *
     * @since 1.0.0
     */
    public Map<Column,?> getBulkUpdateValues() {
        return _strategy.getBulkUpdateValues();
    }
}
