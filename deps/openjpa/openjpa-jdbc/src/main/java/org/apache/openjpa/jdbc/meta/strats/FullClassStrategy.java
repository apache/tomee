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
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for when the class maps all fields to its own table.
 *
 * @author Abe White
 */
public class FullClassStrategy
    extends AbstractClassStrategy {

    public static final String ALIAS = "full";

    private static final Localizer _loc = Localizer.forPackage
        (FullClassStrategy.class);

    public String getAlias() {
        return ALIAS;
    }

    public void map(boolean adapt) {
        if (cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-full", cls));

        ClassMapping sup = cls.getMappedPCSuperclassMapping();
        ClassMappingInfo info = cls.getMappingInfo();
        if (sup != null && info.isJoinedSubclass())
            throw new MetaDataException(_loc.get("not-full", cls));

        info.assertNoJoin(cls, true);
        info.assertNoForeignKey(cls, !adapt);
        info.assertNoIndex(cls, false);
        info.assertNoUnique(cls, false);

        // find class table
        Table table = info.getTable(cls, adapt);

        // find primary key column
        Column[] pkCols = null;
        if (cls.getIdentityType() == cls.ID_DATASTORE) {
            Column id = new Column();
            DBDictionary dict = cls.getMappingRepository().getDBDictionary();
            DBIdentifier idName = DBIdentifier.newColumn("id", dict != null ? dict.delimitAll() : false);
            id.setIdentifier(idName);
            id.setJavaType(JavaTypes.LONG);
            id.setComment("datastore id");
            if (cls.getIdentityStrategy() == ValueStrategies.AUTOASSIGN)
                id.setAutoAssigned(true);
            id.setNotNull(true);
            pkCols = info.getDataStoreIdColumns(cls, new Column[]{ id },
                table, adapt);
            cls.setPrimaryKeyColumns(pkCols);
            cls.setColumnIO(info.getColumnIO());
        }
        cls.setTable(table);

        // add a primary key if we don't have one already
        PrimaryKey pk = table.getPrimaryKey();
        if (pk == null) {
            DBIdentifier pkname = DBIdentifier.NULL;
            if (adapt)
                pkname = cls.getMappingRepository().getMappingDefaults().
                    getPrimaryKeyIdentifier(cls, table);
            pk = table.addPrimaryKey(pkname);
            pk.setLogical(!adapt);
            if (pkCols != null)
                pk.setColumns(pkCols);
        }

        // set joinable
        if (cls.getIdentityType() == ClassMapping.ID_DATASTORE)
            cls.setJoinable(cls.getPrimaryKeyColumns()[0],
                new IdentityJoinable(cls));
    }

    public boolean supportsEagerSelect(Select sel, OpenJPAStateManager sm,
        JDBCStore store, ClassMapping base, JDBCFetchConfiguration fetch) {
        return false;
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_INSERT, sm, true);
        if (cls.getIdentityType() == cls.ID_DATASTORE)
            row.setPrimaryKey(cls.getColumnIO(), sm);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_UPDATE, sm, false);
        if (row != null)
            row.wherePrimaryKey(sm);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(cls.getTable(), Row.ACTION_DELETE, sm, true);
        row.wherePrimaryKey(sm);
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return true;
    }
}
