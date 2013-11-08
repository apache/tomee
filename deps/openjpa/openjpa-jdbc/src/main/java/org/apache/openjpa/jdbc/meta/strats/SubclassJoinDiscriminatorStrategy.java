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

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Discriminator strategy that outer joins to all possible subclass tables
 * to determine the class of an instance. This indicator type should only
 * be used with vertical inheritance hierarchies.
 *
 * @author Abe White
 */
public class SubclassJoinDiscriminatorStrategy
    extends AbstractDiscriminatorStrategy {

    public static final String ALIAS = "subclass-join";

    private static final Localizer _loc = Localizer.forPackage
        (SubclassJoinDiscriminatorStrategy.class);

    public String getAlias() {
        return ALIAS;
    }

    public void map(boolean adapt) {
        ClassMapping cls = disc.getClassMapping();
        if (cls.getJoinablePCSuperclassMapping() != null
            || cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-base-disc", cls));

        DiscriminatorMappingInfo info = disc.getMappingInfo();
        info.assertNoSchemaComponents(disc, true);

        // make sure outer joins are supported
        DBDictionary dict = cls.getMappingRepository().getDBDictionary();
        if (dict.joinSyntax == JoinSyntaxes.SYNTAX_TRADITIONAL)
            throw new MetaDataException(_loc.get("outer-join-support", cls));
    }

    public boolean select(Select sel, ClassMapping mapping) {
        if (isFinal)
            return false;

        // make sure to select our first pk col so that we detect it as
        // non-null in getClass; if we have no superclass we don't need to
        // do this because the base class always selects its pks anyway
        boolean seld = false;
        if (mapping.getPrimaryKeyColumns().length > 0
            && mapping.getJoinablePCSuperclassMapping() != null) {
            sel.select(mapping.getPrimaryKeyColumns()[0]);
            seld = true;
        }

        ClassMapping[] subs = mapping.getJoinablePCSubclassMappings();
        if (subs.length == 0)
            return seld;

        // outer join to each subclass and select its first pk col;
        // the subclass array is already ordered in levels of inheritance, so
        // each subclass only has to join from its direct superclass
        Column[] pks;
        for (int i = 0; i < subs.length; i++) {
            if (subs[i].getJoinablePCSuperclassMapping() == null)
                continue;

            pks = subs[i].getPrimaryKeyColumns();
            if (pks.length > 0) {
                sel.select(pks[0], subs[i].joinSuperclass
                    (sel.newJoins(), true));
                seld = true;
            }
        }
        return seld;
    }

    public Class getClass(JDBCStore store, ClassMapping base, Result res)
        throws SQLException, ClassNotFoundException {
        if (isFinal)
            return base.getDescribedType();

        // find the most derived class with a non-null pk col in the result.
        // note that we don't perform any joins here, taking advantage of the
        // fact that result joins are unnecessary when there is no relation
        // involved; we're cheating a little
        ClassMapping[] subs = base.getJoinablePCSubclassMappings();
        Class derived = base.getDescribedType();
        Column[] pks;
        for (int i = 0; i < subs.length; i++) {
            pks = subs[i].getPrimaryKeyColumns();
            if (pks.length == 0)
                continue;

            // possible that a sibling class cols were already discovered, in
            // which case we can skip this sub
            if (!derived.isAssignableFrom(subs[i].getDescribedType()))
                continue;

            // see if all pk cols are non-null
            if (res.contains(pks[0])
                && res.getObject(pks[0], -1, null) != null)
                derived = subs[i].getDescribedType();
        }
        return derived;
    }

    public boolean hasClassConditions(ClassMapping base, boolean subclasses) {
        if (isFinal || subclasses)
            return false;
        ClassMapping[] subs = base.getJoinablePCSubclassMappings();
        if (subs.length == 0)
            return false;
        return true;
    }

    public SQLBuffer getClassConditions(Select sel, Joins joins, 
        ClassMapping base, boolean subclasses) {
        // add conditions making sure no subclass tables have records for
        // this instance
        ClassMapping[] subs = base.getJoinablePCSubclassMappings();
        SQLBuffer buf = null;
        Column[] pks;
        for (int i = 0; i < subs.length; i++) {
            pks = subs[i].getPrimaryKeyColumns();
            if (pks.length == 0)
                continue;

            if (buf == null) {
                // make sure the base class is aliased first so that we don't
                // end up with our outer joins before the inner ones
                buf = new SQLBuffer(sel.getConfiguration().
                    getDBDictionaryInstance());
                sel.getColumnAlias(base.getPrimaryKeyColumns()[0], joins);
            } else
                buf.append(" AND ");
            buf.append(sel.getColumnAlias(pks[0], joins)).append(" IS NULL");
        }
        return buf;
    }
}
