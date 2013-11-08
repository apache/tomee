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

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.Proxy;

/**
 * Maps a collection of related objects through an association table.
 *
 * @author Abe White
 */
public class RelationCollectionTableFieldStrategy
    extends RelationToManyTableFieldStrategy
    implements LRSCollectionFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (RelationCollectionTableFieldStrategy.class);

    public FieldMapping getFieldMapping() {
        return field;
    }

    public ClassMapping[] getIndependentElementMappings(boolean traverse) {
        return super.getIndependentElementMappings(traverse);
    }

    public Column[] getElementColumns(ClassMapping elem) {
        return field.getElementMapping().getColumns();
    }

    public ForeignKey getJoinForeignKey(ClassMapping elem) {
        return super.getJoinForeignKey(elem);
    }

    public void selectElement(Select sel, ClassMapping elem, JDBCStore store,
        JDBCFetchConfiguration fetch, int eagerMode, Joins joins) {
        super.selectElement(sel, elem, store, fetch, eagerMode, joins);
    }

    public Object loadElement(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Result res, Joins joins)
        throws SQLException {
        return super.loadElement(sm, store, fetch, res, joins);
    }

    public Joins join(Joins joins, ClassMapping elem) {
        return super.join(joins, elem);
    }

    public Joins joinElementRelation(Joins joins, ClassMapping elem) {
        return super.joinElementRelation(joins, elem);
    }

    protected Proxy newLRSProxy() {
        return new LRSProxyCollection(this);
    }

    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.COLLECTION
            && field.getTypeCode() != JavaTypes.ARRAY)
            throw new MetaDataException(_loc.get("not-coll", field));
        super.map(adapt);
    }
    
    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
    	if (!field.isBidirectionalJoinTableMappingOwner())
    		super.insert(sm, store, rm);
    }
    
    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
    	if (!field.isBidirectionalJoinTableMappingOwner())
    		super.update(sm, store, rm);
    }
    
    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
    	if (!field.isBidirectionalJoinTableMappingOwner())
    		super.delete(sm, store, rm);
    }
}
