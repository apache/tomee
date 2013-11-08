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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.meta.strats.NoneVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.SuperclassVersionStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Information about the mapping from a version indicator to the schema, in
 * raw form. The columns and tables used in mapping info will not be part of
 * the {@link SchemaGroup} used at runtime. Rather, they will be structs
 * with the relevant pieces of information filled in.
 *
 * @author Abe White
 */
public class VersionMappingInfo
    extends MappingInfo {

    private static final Localizer _loc = Localizer.forPackage
    	(VersionMappingInfo.class);
    /**
     * Return the columns set for this version, based on the given templates.
     */
    public Column[] getColumns(Version version, Column[] templates,
        boolean adapt) {
    	if (spansMultipleTables(templates))
    		return getMultiTableColumns(version, templates, adapt);
        Table table = getSingleTable(version, templates);
        version.getMappingRepository().getMappingDefaults().populateColumns
            (version, table, templates);
        return createColumns(version, null, templates, table, adapt);
    }
    
    /**
     * Return the columns set for this version when the columns are spread 
     * across multiple tables.
     */
    public Column[] getMultiTableColumns(Version vers, Column[] templates,
            boolean adapt) {
    	Table primaryTable = vers.getClassMapping().getTable();
    	List<DBIdentifier> secondaryTableNames = Arrays.asList(vers
                .getClassMapping().getMappingInfo().getSecondaryTableIdentifiers());
        Map<Table, List<Column>> assign = new LinkedHashMap<Table,
                List<Column>>();
    	for (Column col : templates) {
    	    DBIdentifier tableName = col.getTableIdentifier();
    	    Table table;
    		if (DBIdentifier.isEmpty(tableName) 
    		  || tableName.equals(primaryTable.getIdentifier())) {
    			table = primaryTable;
    		} else if (secondaryTableNames.contains(tableName)) {
    			table = primaryTable.getSchema().getTable(tableName);
    		} else {
                throw new UserException(_loc.get("bad-version-column-table",
    					col.getIdentifier().toString(), tableName));
    		}
    		if (!assign.containsKey(table))
    			assign.put(table, new ArrayList<Column>());
    		assign.get(table).add(col);
    	}
    	MappingDefaults def = vers.getMappingRepository().getMappingDefaults();
    	List<Column> result = new ArrayList<Column>();

        Set<Map.Entry<Table,List<Column>>> assignSet = assign.entrySet();
        for (Map.Entry<Table,List<Column>> assignEntry : assignSet) {
            Table table = assignEntry.getKey();
            List<Column> cols = assignEntry.getValue();
            Column[] partTemplates = cols.toArray(new Column[cols.size()]);
            def.populateColumns(vers, table, partTemplates);
            result.addAll(Arrays.asList(createColumns(vers, null, partTemplates,
    				table, adapt)));
    	}
    	return result.toArray(new Column[result.size()]);
    }
    
    /**
     * Return the index to set on the version columns, or null if none.
     */
    public Index getIndex(Version version, Column[] cols, boolean adapt) {
        Index idx = null;
        if (cols.length > 0)
            idx = version.getMappingRepository().getMappingDefaults().
                getIndex(version, cols[0].getTable(), cols);
        return createIndex(version, null, idx, cols, adapt);
    }

    /**
     * Synchronize internal information with the mapping data for the given
     * version.
     */
    public void syncWith(Version version) {
        clear(false);

        ClassMapping cls = version.getClassMapping();
        Column[] cols = version.getColumns();

        setColumnIO(version.getColumnIO());
        syncColumns(version, cols, false);
        syncIndex(version, version.getIndex());

        if (version.getStrategy() == null
            || version.getStrategy() instanceof SuperclassVersionStrategy)
            return;

        // explicit version strategy if:
        // - unmapped class and version mapped
        // - mapped base class
        // - mapped subclass that doesn't rely on superclass version
        String strat = version.getStrategy().getAlias();
        if ((!cls.isMapped() && !NoneVersionStrategy.ALIAS.equals(strat))
            || (cls.isMapped()
            && cls.getJoinablePCSuperclassMapping() == null))
            setStrategy(strat);
    }
    
    /**
     * Affirms if the given columns belong to more than one tables.
     */
    boolean spansMultipleTables(Column[] cols) {
    	if (cols == null || cols.length <= 1) 
    		return false;
    	Set<DBIdentifier> tables = new HashSet<DBIdentifier>();
    	for (Column col : cols)
    		if (tables.add(col.getTableIdentifier()) && tables.size() > 1)
    			return true;
    	return false;
    }
    
    /**
     * Gets the table where this version columns are mapped.
     */
    private Table getSingleTable(Version version, Column[] cols) {
    	if (cols == null || cols.length == 0 
    	 || DBIdentifier.isEmpty(cols[0].getTableIdentifier()))
    		return version.getClassMapping().getTable();
    	return version.getClassMapping().getTable().getSchema()
    		.getTable(cols[0].getTableIdentifier());
    }


}
