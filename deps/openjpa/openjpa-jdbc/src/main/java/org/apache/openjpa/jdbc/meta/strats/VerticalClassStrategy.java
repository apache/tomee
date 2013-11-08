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
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.DelegatingJoinable;
import org.apache.openjpa.jdbc.meta.Joinable;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for subclasses that join to their superclass table.
 *
 * @author Abe White
 */
public class VerticalClassStrategy
    extends AbstractClassStrategy {

    public static final String ALIAS = "vertical";

    private static final Localizer _loc = Localizer.forPackage
        (VerticalClassStrategy.class);

    private Boolean _fkOid = null;

    public String getAlias() {
        return ALIAS;
    }

    public void map(boolean adapt) {
        ClassMapping sup = cls.getMappedPCSuperclassMapping();
        if (sup == null)
            throw new MetaDataException(_loc.get("not-sub", cls));

        ClassMappingInfo info = cls.getMappingInfo();
        info.assertNoIndex(cls, false);
        info.assertNoUnique(cls, false);

        // foreign key from this class to our superclass
        Table table = info.getTable(cls, adapt);
        ForeignKey fk = info.getSuperclassJoin(cls, table, adapt);
        Column[] pkCols = fk.getColumns();

        cls.setTable(table);
        cls.setJoinForeignKey(fk);
        cls.setPrimaryKeyColumns(pkCols);
        cls.setColumnIO(info.getColumnIO());

        // add a primary key if we don't have one already
        PrimaryKey pk = table.getPrimaryKey();
        if (pk == null) {
            DBIdentifier pkname = DBIdentifier.NULL;
            if (adapt)
                pkname = cls.getMappingRepository().getMappingDefaults().
                    getPrimaryKeyIdentifier(cls, table);
            pk = table.addPrimaryKey(pkname);
            pk.setLogical(!adapt);
            pk.setColumns(pkCols);
        }

        // map joinables
        Joinable join;
        Column[] cols;
        for (int i = 0; i < pkCols.length; i++) {
            // may have already registered a multi-column joinable
            if (cls.getJoinable(pkCols[i]) != null)
                continue;

            // create joinable that delegates all methods to the superclass'
            // joinable, but changes the columns as appropriate using
            // our superclass join fk
            join = sup.assertJoinable(fk.getPrimaryKeyColumn(pkCols[i]));
            join = new DelegatingJoinable(join, fk);
            cols = join.getColumns();
            for (int j = 0; j < cols.length; j++)
                cls.setJoinable(cols[j], join);
        }
    }

    public void initialize() {
        // this requires all fields to be resolved, so delay it until init
        ClassMapping sup = cls.getMappedPCSuperclassMapping();
        _fkOid = sup.isForeignKeyObjectId(cls.getJoinForeignKey());
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_INSERT, sm, true);
        row.setPrimaryKey(cls.getColumnIO(), sm);
        row.setForeignKey(cls.getJoinForeignKey(), cls.getColumnIO(), sm);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_UPDATE, sm, false);
        if (row != null) {
            row.wherePrimaryKey(sm);
            row.whereForeignKey(cls.getJoinForeignKey(), sm);
        }
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_DELETE, sm, true);
        row.wherePrimaryKey(sm);
        row.whereForeignKey(cls.getJoinForeignKey(), sm);
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return Boolean.TRUE.equals(_fkOid) || (!hasAll && _fkOid == null);
    }

    public Joins joinSuperclass(Joins joins, boolean toThis) {
        if (toThis)
            return joins.outerJoin(cls.getJoinForeignKey(), true, false);
        return joins.join(cls.getJoinForeignKey(), false, false);
    }

    public boolean supportsEagerSelect(Select sel, OpenJPAStateManager sm,
        JDBCStore store, ClassMapping base, JDBCFetchConfiguration fetch) {
        return store.getDBDictionary().canOuterJoin(sel.getJoinSyntax(),
            cls.getJoinForeignKey());
    }
}
