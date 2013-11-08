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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;

/**
 * An abstract container mapping that handles traversing the
 * join to examine the size of the relation.
 *
 * @author Marc Prud'hommeaux
 */
public abstract class ContainerFieldStrategy
    extends AbstractFieldStrategy {

    /**
     * Return all independent mappings to which this strategy must join in
     * order to access collection elements, or empty array if none.
     *
     * @param traverse whether we're traversing through to the related type
     * @see ValueMapping#getIndependentTypeMappings
     * @see ClassMapping#EMPTY_MAPPINGS
     */
    protected abstract ClassMapping[] getIndependentElementMappings
        (boolean traverse);

    public void appendIsEmpty(SQLBuffer sql, Select sel, Joins joins) {
        testEmpty(sql, sel, joins, true);
    }

    public void appendIsNotEmpty(SQLBuffer sql, Select sel, Joins joins) {
        testEmpty(sql, sel, joins, false);
    }

    public void appendIsNull(SQLBuffer sql, Select sel, Joins joins) {
        testEmpty(sql, sel, joins, true);
    }

    public void appendIsNotNull(SQLBuffer sql, Select sel, Joins joins) {
        testEmpty(sql, sel, joins, false);
    }

    /**
     * Appends SQL for a sub-select testing whether the container is empty.
     */
    private void testEmpty(SQLBuffer sql, Select sel, Joins joins,
        boolean empty) {
        if (empty)
            sql.append("0 = ");
        else
            sql.append("0 < ");

        appendSize(sql, sel, joins);
    }

    public abstract ForeignKey getJoinForeignKey();

    public void appendSize(SQLBuffer sql, Select sel, Joins joins) {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        dict.assertSupport(dict.supportsSubselect, "SupportsSubselect");

        ClassMapping[] ind = getIndependentElementMappings(false);
        if (ind != null && ind.length > 1)
            throw RelationStrategies.unjoinable(field);

        ForeignKey fk = getJoinForeignKey();
        appendJoinCount(sql, sel, joins, dict, field, fk);
    }

    public void appendIndex(SQLBuffer sql, Select sel, Joins joins) {
        sql.append(sel.getColumnAlias(field.getOrderColumn(),
            field.getName()));
    }

    protected static void appendJoinCount(SQLBuffer sql, Select sel,
        Joins joins, DBDictionary dict, FieldMapping field, ForeignKey fk) {
        String fullTable = dict.getFullName(fk.getTable(), false);
        sql.append("(SELECT COUNT(*) FROM ").append(fullTable).
            append(" WHERE ");
        appendUnaliasedJoin(sql, sel, joins, dict, field, fk);
        sql.append(")");
    }

    public static void appendUnaliasedJoin(SQLBuffer sql, Select sel,
        Joins joins, DBDictionary dict, FieldMapping field, ForeignKey fk) {
        String fullTable = dict.getFullName(fk.getTable(), false);

        Column[] cols = fk.getColumns();
        Column[] pks = fk.getPrimaryKeyColumns();
        int count = 0;
        for (int i = 0; i < cols.length; i++, count++) {
            if (count > 0)
                sql.append(" AND ");
            sql.append(fullTable).append(".").append(cols[i]).append(" = ").
                append(sel.getColumnAlias(pks[i], joins));
        }

        cols = fk.getConstantColumns();
        for (int i = 0; i < cols.length; i++, count++) {
            if (count > 0)
                sql.append(" AND ");
            sql.append(fullTable).append(".").append(cols[i]).append(" = ").
                appendValue(fk.getConstant(cols[i]), cols[i]);
        }

        pks = fk.getConstantPrimaryKeyColumns();
        for (int i = 0; i < pks.length; i++, count++) {
            if (count > 0)
                sql.append(" AND ");
            sql.append(sel.getColumnAlias(pks[i], joins)).append(" = ").
                appendValue(fk.getPrimaryKeyConstant(pks[i]), pks[i]);
        }
    }
}
