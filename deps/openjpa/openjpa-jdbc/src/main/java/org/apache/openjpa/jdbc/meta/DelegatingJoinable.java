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

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Delegates to another {@link Joinable} after doing column
 * translation. Useful for joins that are not managed by any fields on
 * the joined-from side. For example, a vertical class mapping manages
 * foreign key columns linking to the superclass table. It needs to use
 * the joinables of the superclass to actually extract the column values
 * from field values and so forth, but in order for other mappings to join
 * to its foreign key columns it needs joinables of its own. So it creates
 * delegating joinables.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class DelegatingJoinable
    implements Joinable {

    private static final Localizer _loc = Localizer.forPackage
        (DelegatingJoinable.class);

    private final Joinable _join;
    private final ForeignKey _fk;
    private final Column[] _cols;

    /**
     * Constructor.
     *
     * @param join the mapping to delegate to
     * @param fk the foreign key to use to translate any given columns
     * to the given delegate's columns
     */
    public DelegatingJoinable(Joinable join, ForeignKey fk) {
        _join = join;
        _fk = fk;

        Column[] pks = join.getColumns();
        _cols = new Column[pks.length];
        for (int i = 0; i < pks.length; i++) {
            _cols[i] = fk.getColumn(pks[i]);
            if (_cols[i] == null)
                throw new MetaDataException(_loc.get("incomplete-join",
                    pks[i].getFullDBIdentifier()));
        }
    }

    /**
     * Constructor.
     *
     * @param join the mapping to delegate to
     * @param cols translated columns
     */
    public DelegatingJoinable(Joinable join, Column[] cols) {
        _join = join;
        _fk = null;
        _cols = cols;
        if (cols.length != join.getColumns().length)
            throw new MetaDataException(_loc.get("bad-remap",
                join.getColumns()[0].getFullDBIdentifier()));
    }

    public int getFieldIndex() {
        return _join.getFieldIndex();
    }

    public Object getPrimaryKeyValue(Result res, Column[] cols, ForeignKey fk, 
        JDBCStore store, Joins joins)
        throws SQLException {
        return _join.getPrimaryKeyValue(res, cols, fk, store, joins);
    }

    public Column[] getColumns() {
        return _cols;
    }

    public Object getJoinValue(Object val, Column col, JDBCStore store) {
        return _join.getJoinValue(val, translate(col), store);
    }

    public Object getJoinValue(OpenJPAStateManager sm, Column col,
        JDBCStore store) {
        return _join.getJoinValue(sm, translate(col), store);
    }

    public void setAutoAssignedValue(OpenJPAStateManager sm, JDBCStore store,
        Column col, Object autogen) {
        _join.setAutoAssignedValue(sm, store, translate(col), autogen);
    }

    private Column translate(Column col) {
        if (_fk != null)
            return _fk.getPrimaryKeyColumn(col);

        Column[] cols = _join.getColumns();
        for (int i = 0; i < cols.length; i++)
            if (col == cols[i])
                return _cols[i];
        return null;
    }
}
