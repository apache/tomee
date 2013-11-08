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
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Base discriminator strategy that determines the class of database
 * records using a column holding a value mapped to a class, and limits
 * SELECTs using an IN (...) statement.
 *
 * @author Abe White
 */
public abstract class InValueDiscriminatorStrategy
    extends AbstractDiscriminatorStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (InValueDiscriminatorStrategy.class);

    /**
     * Return the Java type code from {@link JavaTypes} for the discriminator
     * values. This method is only used during mapping installation.
     */
    protected abstract int getJavaType();

    /**
     * Return the discriminator value for the given type.
     */
    protected abstract Object getDiscriminatorValue(ClassMapping cls);

    /**
     * Convert the given discriminator value to the corresponding class.
     */
    protected abstract Class getClass(Object val, JDBCStore store)
        throws ClassNotFoundException;

    public void map(boolean adapt) {
        ClassMapping cls = disc.getClassMapping();
        if (cls.getJoinablePCSuperclassMapping() != null
            || cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-base-disc", cls));

        DiscriminatorMappingInfo info = disc.getMappingInfo();
        info.assertNoJoin(disc, true);
        info.assertNoForeignKey(disc, !adapt);
        info.assertNoUnique(disc, false);

        Column tmplate = new Column();
        tmplate.setJavaType(getJavaType());
        DBDictionary dict = cls.getMappingRepository().getDBDictionary();
        DBIdentifier typName = DBIdentifier.newColumn("typ", dict != null ? dict.delimitAll() : false);
        tmplate.setIdentifier(typName);

        Column[] cols = info.getColumns(disc, new Column[]{ tmplate }, adapt);
        disc.setColumns(cols);
        disc.setColumnIO(info.getColumnIO());

        Index idx = info.getIndex(disc, cols, adapt);
        disc.setIndex(idx);
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        Row row = rm.getRow(disc.getClassMapping().getTable(),
            Row.ACTION_INSERT, sm, true);
        Object cls = getDiscriminatorValue((ClassMapping) sm.getMetaData());
        if (disc.getColumnIO().isInsertable(0, cls == null))
            row.setObject(disc.getColumns()[0], cls);
    }

    public boolean select(Select sel, ClassMapping mapping) {
        if (isFinal)
            return false;
        sel.select(disc.getColumns());
        return true;
    }

    public Class getClass(JDBCStore store, ClassMapping base, Result res)
        throws SQLException, ClassNotFoundException {
        if (isFinal 
                || !useDiscrimColumn(base, res)
                || (base.getPCSuperclass() == null && base.getJoinablePCSubclassMappings().length == 0)) {
            return base.getDescribedType();
        }

        Object cls = res.getObject(disc.getColumns()[0], disc.getJavaType(), null);
        return getClass(cls, store);
    }
    
    private final boolean useDiscrimColumn(ClassMapping base, Result res) throws SQLException {
        if (res.getBaseMapping() != null && base != null) {
            // check whether the result type is assignable to the base mapping.
            // if not assignable the discriminator value will not be correct.
            if (!base.getDescribedType().isAssignableFrom(res.getBaseMapping().getDescribedType())) {
                return false;
            }
        }
        return res.contains(disc.getColumns()[0]);
    }

    public boolean hasClassConditions(ClassMapping base, boolean subclasses) {
        // if selecting the first mapped class and all subclasses, no need
        // to limit the query
        if (isFinal || (base.getJoinablePCSuperclassMapping() == null
            && subclasses))
            return false;

        // if no subclasses or superclass, no need for conditions
        ClassMapping[] subs = base.getJoinablePCSubclassMappings();
        if (subs.length == 0 && base.getJoinablePCSuperclassMapping() == null)
            return false;

        return true;
    }

    public SQLBuffer getClassConditions(Select sel, Joins joins, 
        ClassMapping base, boolean subclasses) {
        Column col = disc.getColumns()[0];
        SQLBuffer sql = new SQLBuffer(sel.getConfiguration().
            getDBDictionaryInstance());
        String alias = sel.getColumnAlias(col, joins);
        boolean outer = joins != null && joins.isOuter();
        if (outer)
            sql.append("(");
        sql.append(alias);

        // if not selecting subclasses, limit to just the given class
        ClassMapping[] subs = base.getJoinablePCSubclassMappings();
        if (!outer && (!subclasses || subs.length == 0))
            return sql.append(" = ").appendValue(getDiscriminatorValue(base),
                col);

        if (outer)
            sql.append(" IS ").appendValue(null).append(" OR ").append(alias);
        sql.append(" IN (");
        sql.appendValue(getDiscriminatorValue(base), col);
        for (int i = 0; i < subs.length; i++)
            sql.append(", ").appendValue(getDiscriminatorValue(subs[i]), col);
        sql.append(")");
        if (outer)
            sql.append(")");
        return sql;
    }
}

