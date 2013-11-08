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
import java.util.Collection;
import java.util.List;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.xml.Commentable;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataContext;
import org.apache.openjpa.util.MetaDataException;

/**
 * Information about the mapping from a field to the schema, in raw form.
 * The columns and tables used in mapping info will not be part of the
 * {@link SchemaGroup} used at runtime. Rather, they will be structs
 * with the relevant pieces of information filled in.
 *
 * @author Abe White
 * @author Pinaki Poddar
 */
@SuppressWarnings("serial")
public class FieldMappingInfo
    extends MappingInfo
    implements Commentable {

    private static final Localizer _loc = Localizer.forPackage
        (FieldMappingInfo.class);

    private DBIdentifier _tableName = DBIdentifier.NULL;
    private boolean _outer = false;
    private Column _orderCol = null;
    private boolean _canOrderCol = true;
    private String[] _comments = null;
    private List<Unique> _joinTableUniques; // Unique constraints on JoinTable

    /**
     * The user-supplied name of the table for this field.
     * @deprecated
     */
    public String getTableName() {
        return getTableIdentifier().getName();
    }

    public DBIdentifier getTableIdentifier() {
        return _tableName == null ? DBIdentifier.NULL : _tableName;
    }

    /**
     * The user-supplied name of the table for this field.
     * @deprecated
     */
    public void setTableName(String tableName) {
        setTableIdentifier(DBIdentifier.newTable(tableName));
    }

    public void setTableIdentifier(DBIdentifier tableName) {
        _tableName = tableName;
    }

    /**
     * Whether the field's table is joined to the class table through an
     * outer join.
     */
    public boolean isJoinOuter() {
        return _outer;
    }

    /**
     * Whether the field's table is joined to the class table through an
     * outer join.
     */
    public void setJoinOuter(boolean outer) {
        _outer = outer;
    }

    /**
     * Raw synthetic ordering column.
     */
    public Column getOrderColumn() {
        return _orderCol;
    }

    /**
     * Raw synthetic ordering column.
     */
    public void setOrderColumn(Column order) {
        _orderCol = order;
    }

    /**
     * Whether we can have an ordering column.
     */
    public boolean canOrderColumn() {
        return _canOrderCol;
    }

    /**
     * Whether we can have an ordering column.
     */
    public void setCanOrderColumn(boolean canOrder) {
        _canOrderCol = canOrder;
    }

    /**
     * Return the table for the given field, or null if no table given.
     */
    public Table getTable(final FieldMapping field, boolean create,
        boolean adapt) {
        if (DBIdentifier.isNull(_tableName) && !create)
            return null;

        Table table = field.getDefiningMapping().getTable();
        DBIdentifier schemaName = (table == null) ? DBIdentifier.NULL 
            : table.getSchema().getIdentifier();

        // if we have no join columns defined, there may be class-level join
        // information with a more fully-qualified name for our table
        DBIdentifier tableName = _tableName;
        if (!DBIdentifier.isNull(tableName) && getColumns().isEmpty())
            tableName = field.getDefiningMapping().getMappingInfo().
                getSecondaryTableIdentifier(tableName);

        return createTable(field, new TableDefaults() {
            public String get(Schema schema) {
                // delay this so that we don't do schema reflection for unique
                // table name unless necessary
                return field.getMappingRepository().getMappingDefaults().
                    getTableName(field, schema);
            }
            public DBIdentifier getIdentifier(Schema schema) {
                // TODO Auto-generated method stub
                return field.getMappingRepository().getMappingDefaults().
                    getTableIdentifier(field, schema);
            }
        }, schemaName, tableName, adapt);
    }

    public ForeignKey getJoinForeignKey (final FieldMapping field, Table table,
        boolean adapt) {
        if (field.isUni1ToMFK()) {
            List<Column> cols = field.getElementMapping().getValueInfo().getColumns();
            return getJoin(field, table, adapt, cols);
        }
        return null;
    }
    
    /**
     * Return the join from the field table to the owning class table.
     */
    public ForeignKey getJoin(final FieldMapping field, Table table,
        boolean adapt) {
        // if we have no join columns defined, check class-level join
    	// if the given field is embedded then consider primary table of owner
        return getJoin(field, table, adapt, getColumns());
    }
    
    public ForeignKey getJoin(final FieldMapping field, Table table,
            boolean adapt, List<Column> cols) {
        if (cols.isEmpty()) {
        	ClassMapping mapping;
        	if (field.isEmbedded() && 
                    field.getDeclaringMapping().getEmbeddingMapping() != null) {
                mapping = field.getDeclaringMapping().getEmbeddingMapping()
        			.getFieldMapping().getDeclaringMapping();
        	} else {
        		mapping = field.getDefiningMapping();
        	}
            cols = mapping.getMappingInfo().
                getSecondaryTableJoinColumns(_tableName);
        }
        ForeignKeyDefaults def = new ForeignKeyDefaults() {
            public ForeignKey get(Table local, Table foreign, boolean inverse) {
                return field.getMappingRepository().getMappingDefaults().
                    getJoinForeignKey(field, local, foreign);
            }

            public void populate(Table local, Table foreign, Column col,
                Object target, boolean inverse, int pos, int cols) {
                field.getMappingRepository().getMappingDefaults().
                    populateJoinColumn(field, local, foreign, col, target,
                        pos, cols);
            }
        };
        ClassMapping cls = getDefiningMapping(field);
        return createForeignKey(field, "join", cols, def, table, cls, cls,
            false, adapt);
    }
    
    private ClassMapping getDefiningMapping(FieldMapping field) {
        ClassMapping clm = field.getDefiningMapping();
        ValueMappingImpl value = (ValueMappingImpl)clm.getEmbeddingMetaData();
        if (value == null)
            return clm;
        FieldMapping field1 = value.getFieldMapping();
        return getDefiningMapping(field1);
    }
    
    /**
     * Unique constraint on the field join.
     */
    public Unique getJoinUnique(FieldMapping field, boolean def,
        boolean adapt) {
        ForeignKey fk = field.getJoinForeignKey();
        if (fk == null)
            return null;

        Unique unq = null;
        if (fk.getColumns().length > 0)
            unq = field.getMappingRepository().getMappingDefaults().
                getJoinUnique(field, fk.getTable(), fk.getColumns());
        return createUnique(field, "join", unq, fk.getColumns(), adapt);
    }
    
    /**
     * Add Unique Constraint to the Join Table.
     */
    public void addJoinTableUnique(Unique u) {
    	if (_joinTableUniques == null)
    		_joinTableUniques = new ArrayList<Unique>();
    	_joinTableUniques.add(u);
    }
    
    /**
     * Get the unique constraints associated with the Sequence table.
     */
    public Unique[] getJoinTableUniques(FieldMapping field, boolean def, 
    		boolean adapt) {
        return getUniques(field, _joinTableUniques, def, adapt);
    }   
    
    private Unique[] getUniques(FieldMapping field, List<Unique> uniques, 
    		boolean def, boolean adapt) {
        if (uniques == null || uniques.isEmpty())
            return new Unique[0];
        Collection<Unique> result = new ArrayList<Unique>();
        for (Unique template : uniques) {
            Column[] templateColumns = template.getColumns();
            Column[] uniqueColumns = new Column[templateColumns.length];
            Table table = getTable(field, true, adapt);
            for (int i=0; i<uniqueColumns.length; i++) {
                DBIdentifier columnName = templateColumns[i].getIdentifier();
                Column uniqueColumn = table.getColumn(columnName);
                uniqueColumns[i] = uniqueColumn;
            }
            Unique unique = createUnique(field, "unique", template,  
                uniqueColumns, adapt);
            if (unique != null)
                result.add(unique);
        }
        return result.toArray(new Unique[result.size()]);
    }   
    
   /**
     * Index on the field join.
     */
    public Index getJoinIndex(FieldMapping field, boolean adapt) {
        ForeignKey fk = field.getJoinForeignKey();
        if (fk == null)
            return null;

        Index idx = null;
        if (fk.getColumns().length > 0)
            idx = field.getMappingRepository().getMappingDefaults().
                getJoinIndex(field, fk.getTable(), fk.getColumns());
        return createIndex(field, "join", idx, fk.getColumns(), adapt);
    }

    /**
     * Return the ordering column for this field, or null if none.
     */
    public Column getOrderColumn(FieldMapping field, Table table,
        boolean adapt) {
        if (_orderCol != null && field.getOrderDeclaration() != null)
            throw new MetaDataException(_loc.get("order-conflict", field));

        // reset IO
        setColumnIO(null);

        // has user has explicitly turned ordering off?
        if (!_canOrderCol || field.getOrderDeclaration() != null)
            return null;

        // if no defaults return null
        MappingDefaults def = field.getMappingRepository().
            getMappingDefaults();
        if (_orderCol == null && (!adapt && !def.defaultMissingInfo()))
            return null;

        Column tmplate = new Column();
        // Compatibility option determines what should be used for
        // the default order column name
        boolean delimit = field.getMappingRepository().getDBDictionary().delimitAll();
        if (field.getMappingRepository().getConfiguration()
            .getCompatibilityInstance().getUseJPA2DefaultOrderColumnName()) {
            // Use the same strategy as column to build the field name
            DBIdentifier sName = DBIdentifier.newColumn(field.getName(), delimit);
            sName = DBIdentifier.append(sName,"_ORDER");
            tmplate.setIdentifier(sName);
        } else {
            tmplate.setIdentifier(DBIdentifier.newColumn("ordr", delimit));
        }
        
        tmplate.setJavaType(JavaTypes.INT);
        if (!def.populateOrderColumns(field, table, new Column[]{ tmplate })
            && _orderCol == null)
            return null;

        if (_orderCol != null) {
            ColumnIO io = new ColumnIO();
            io.setNullInsertable(0, !_orderCol.isNotNull());
            io.setNullUpdatable(0, !_orderCol.isNotNull());
            io.setInsertable(0, !_orderCol.getFlag(Column.FLAG_UNINSERTABLE));
            io.setUpdatable(0, !_orderCol.getFlag(Column.FLAG_UNUPDATABLE));
            setColumnIO(io);
        }

        return mergeColumn(field, "order", tmplate, true, _orderCol, table,
            adapt, def.defaultMissingInfo());
    }

    /**
     * Synchronize internal information with the mapping data for the given
     * field.
     */
    public void syncWith(FieldMapping field) {
        clear(false);

        if (field.getJoinForeignKey() != null)
            _tableName = field.getMappingRepository().getDBDictionary().
                getFullIdentifier(field.getTable(), true);

        ClassMapping def = field.getDefiningMapping();
        setColumnIO(field.getJoinColumnIO());
        if (field.getJoinForeignKey() != null && def.getTable() != null)
            syncForeignKey(field, field.getJoinForeignKey(),
                field.getTable(), def.getTable());
        _outer = field.isJoinOuter();

        syncIndex(field, field.getJoinIndex());
        syncUnique(field, field.getJoinUnique());
        syncJoinTableUniques(field, field.getJoinTableUniques());
        syncOrderColumn(field);
        syncStrategy(field);
    }

    /**
     * Synchronize internal mapping strategy information with the given field.
     */
    public void syncStrategy(FieldMapping field) {
        setStrategy(null);
        if (field.getHandler() != null || field.getStrategy() == null)
            return;

        // explicit strategy if the strategy isn't the expected default
        Strategy strat = field.getMappingRepository().defaultStrategy
            (field, false);
        if (strat == null || !strat.getAlias().equals(field.getAlias()))
            setStrategy(field.getAlias());
    }

    /**
     * Synchronize internal order column information with the given field.
     */
    public void syncOrderColumn(FieldMapping field) {
        if (field.getOrderColumn() != null)
            _orderCol = syncColumn(field, field.getOrderColumn(), 1, false,
                field.getTable(), null, null, false);
        else
            _orderCol = null;
    }
    
    /**
     * Sets internal constraint information to match given mapped constraint.
     */
    protected void syncJoinTableUniques(MetaDataContext context, Unique[] unqs)
    {
        if (unqs == null) {
            _joinTableUniques = null;
            return;
        }
        _joinTableUniques = new ArrayList<Unique>();
        for (Unique unique:unqs) {
        	Unique copy = new Unique();
        	copy.setIdentifier(unique.getIdentifier());
        	copy.setDeferred(unique.isDeferred());
        	_joinTableUniques.add(unique);
        }
    }


    public boolean hasSchemaComponents() {
        return super.hasSchemaComponents() || !DBIdentifier.isNull(_tableName)
            || _orderCol != null;
    }

    protected void clear(boolean canFlags) {
        super.clear(canFlags);
        _tableName = DBIdentifier.NULL;
        _orderCol = null;
        if (canFlags)
            _canOrderCol = true;
    }

    public void copy(MappingInfo info) {
        super.copy(info);
        if (!(info instanceof FieldMappingInfo))
            return;

        FieldMappingInfo finfo = (FieldMappingInfo) info;
        if (DBIdentifier.isNull(_tableName))
            _tableName = finfo.getTableIdentifier();
        if (!_outer)
            _outer = finfo.isJoinOuter();
        if (_canOrderCol && _orderCol == null)
            _canOrderCol = finfo.canOrderColumn();
        if (_canOrderCol && finfo.getOrderColumn() != null) {
            if (_orderCol == null)
                _orderCol = new Column();
            _orderCol.copy(finfo.getOrderColumn());
        }
    }

    public String[] getComments() {
        return (_comments == null) ? EMPTY_COMMENTS : _comments;
    }

    public void setComments(String[] comments) {
        _comments = comments;
    }
}
