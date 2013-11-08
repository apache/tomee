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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.xml.Commentable;
import org.apache.openjpa.meta.MetaDataContext;
import org.apache.openjpa.util.UserException;

/**
 * Information about the mapping from a class to the schema, in raw form.
 * The columns and tables used in mapping info will not be part of the
 * {@link SchemaGroup} used at runtime. Rather, they will be structs
 * with the relevant pieces of information filled in.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class ClassMappingInfo
    extends MappingInfo
    implements SourceTracker, Commentable {

    private static final Localizer _loc = Localizer.forPackage
        (ClassMappingInfo.class);

    private String _className = Object.class.getName();
    private DBIdentifier _tableName = DBIdentifier.NULL;
    private DBIdentifier _schemaName = DBIdentifier.NULL;
    private boolean _joined = false;
    private Map<DBIdentifier, List<Column>> _seconds = null;
    private String _subStrat = null;
    private File _file = null;
    private int _srcType = SRC_OTHER;
    private String[] _comments = null;
    private int _lineNum = 0;  
    private int _colNum = 0;  
    
    // Unique constraints indexed by primary or secondary table name
    private Map<DBIdentifier,List<Unique>> _uniques;

    /**
     * The described class name.
     */
    public String getClassName() {
        return _className;
    }

    /**
     * The described class name.
     */
    public void setClassName(String name) {
        _className = name;
    }

    /**
     * The default strategy for subclasses in this hierarchy.
     */
    public String getHierarchyStrategy() {
        return _subStrat;
    }

    /**
     * The default strategy for subclasses in this hierarchy.
     */
    public void setHierarchyStrategy(String strategy) {
        _subStrat = strategy;
    }

    /**
     * The given table name.
     * @deprecated
     */
    public String getTableName() {
        return getTableIdentifier().getName();
    }

    public DBIdentifier getTableIdentifier() {
        return _tableName == null ? DBIdentifier.NULL : _tableName;
    }

    /**
     * The given table name.
     * @deprecated
     */
    public void setTableName(String table) {
        setTableIdentifier(DBIdentifier.newTable(table));
    }

    public void setTableIdentifier(DBIdentifier table) {
        _tableName = table;
    }

    /**
     * The default schema name for unqualified tables.
     * @deprecated
     */
    public String getSchemaName() {
        return getSchemaIdentifier().getName();
    }

    public DBIdentifier getSchemaIdentifier() {
        return _schemaName == null ? DBIdentifier.NULL : _schemaName;
    }

    /**
     * The default schema name for unqualified tables.
     * @deprecated
     */
    public void setSchemaName(String schema) {
        setSchemaIdentifier(DBIdentifier.newSchema(schema));
    }

    public void setSchemaIdentifier(DBIdentifier schema) {
        _schemaName = schema;
    }

    /**
     * Whether there is a join to the superclass table.
     */
    public boolean isJoinedSubclass() {
        return _joined;
    }

    /**
     * Whether there is a join to the superclass table.
     */
    public void setJoinedSubclass(boolean joined) {
        _joined = joined;
    }

    /**
     * Return the class-level joined tables.
     * @deprecated
     */
    public String[] getSecondaryTableNames() {
        if (_seconds == null)
            return new String[0];
        return DBIdentifier.toStringArray(_seconds.keySet().toArray(new DBIdentifier[]{ }));
    }

    public DBIdentifier[] getSecondaryTableIdentifiers() {
        if (_seconds == null)
            return new DBIdentifier[0];
        return (DBIdentifier[]) _seconds.keySet().toArray(new DBIdentifier[_seconds.size()]);
    }

    /**
     * We allow fields to reference class-level joins using just the table
     * name, whereas the class join might have schema, etc information.
     * This method returns the name of the given table as listed in a
     * class-level join, or the given name if no join exists.
     * @deprecated
     */
    public String getSecondaryTableName(String tableName) {
        return getSecondaryTableIdentifier(DBIdentifier.newTable(tableName)).getName();
    }

    public DBIdentifier getSecondaryTableIdentifier(DBIdentifier tableName) {
        // if no secondary table joins, bad table name, exact match,
        // or an already-qualified table name, nothing to do
        QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(tableName);
        if (_seconds == null || DBIdentifier.isNull(tableName)
            || _seconds.containsKey(tableName)
            || !DBIdentifier.isNull(path.getSchemaName()))
            return tableName;

        // decide which class-level join table is best match
        DBIdentifier best = tableName;
        int pts = 0;
        DBIdentifier fullJoin = DBIdentifier.NULL;
        DBIdentifier join = DBIdentifier.NULL;
        for (Iterator<DBIdentifier> itr = _seconds.keySet().iterator(); itr.hasNext();) {
            // award a caseless match without schema 2 points
            fullJoin = (DBIdentifier) itr.next();
            QualifiedDBIdentifier joinPath = QualifiedDBIdentifier.getPath(fullJoin);
            if (joinPath.isUnqualifiedObject() && pts < 2 && fullJoin.equalsIgnoreCase(tableName)) {
                best = fullJoin;
                pts = 2;
            } else if (joinPath.isUnqualifiedObject())
                continue;

            // immediately return an exact match with schema
            join = joinPath.getIdentifier();
            if (join.equals(tableName))
                return fullJoin;

            // caseless match with schema worth 1 point
            if (pts < 1 && join.equalsIgnoreCase(tableName)) {
                best = fullJoin;
                pts = 1;
            }
        }
        return best;
    }

    /**
     * Return any columns defined for the given class level join, or empty
     * list if none.
     * @deprecated
     */
    public List<Column> getSecondaryTableJoinColumns(String tableName) {
        return getSecondaryTableJoinColumns(DBIdentifier.newTable(tableName));
    }

    public List<Column> getSecondaryTableJoinColumns(DBIdentifier tableName) {
        if (_seconds == null || DBIdentifier.isNull(tableName)) {
            return Collections.emptyList();
        }

        // get the columns for the join with the best match for table name
        List<Column> cols = _seconds.get(getSecondaryTableIdentifier(tableName));
        if (cols == null) {
            // possible that given table has extra info the join table
            // doesn't have; strip it
            QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(tableName);
            if (!DBIdentifier.isNull(path.getSchemaName())) {
                tableName = path.getIdentifier();
                cols = _seconds.get(getSecondaryTableIdentifier(tableName));
            }
        }
        if (cols == null) {
            return Collections.emptyList();
        }
        return cols;
    }
    
    /**
     * Adds a Secondary table of given name to this mapping. A secondary table 
     * must be known before unique constraints are added to a Secondary table.
     * @deprecated
     */
    public void addSecondaryTable(String second) {
    	setSecondaryTableJoinColumns(DBIdentifier.newTable(second), null);
    }

    public void addSecondaryTable(DBIdentifier second) {
        setSecondaryTableJoinColumns(second, null);
    }

    /**
     * Declare the given class-level join to the named (secondary) table.
     * @deprecated
     */
    public void setSecondaryTableJoinColumns(String tableName, List<Column> cols) {
        if (cols == null)
            cols = Collections.emptyList();
        setSecondaryTableJoinColumns(DBIdentifier.newTable(tableName), cols);
    }

    public void setSecondaryTableJoinColumns(DBIdentifier tableName, List<Column> cols) {
        if (cols == null)
            cols = Collections.emptyList();
        if (_seconds == null)
            _seconds = new LinkedHashMap<DBIdentifier, List<Column>>();
        _seconds.put(tableName, cols);
    }
    
    /**
     * Return the named table for the given class.
     * @deprecated
     */
    public Table getTable(final ClassMapping cls, String tableName, 
        boolean adapt) {
        return getTable(cls, DBIdentifier.newTable(tableName), adapt);
    }

    /**
     * Return the named table for the given class.
     */
    public Table getTable(final ClassMapping cls, DBIdentifier tableName, 
    		boolean adapt) {
        Table t = createTable(cls, new TableDefaults() {
            public String get(Schema schema) {
                // delay this so that we don't do schema reflection for unique
                // table name unless necessary
                return cls.getMappingRepository().getMappingDefaults().
                    getTableName(cls, schema);
            }
            public DBIdentifier getIdentifier(Schema schema) {
                return cls.getMappingRepository().getMappingDefaults().
                    getTableIdentifier(cls, schema);
            }
        }, _schemaName, tableName, adapt);
        t.setComment(cls.getTypeAlias() == null
            ? cls.getDescribedType().getName()
            : cls.getTypeAlias());
        return t;
    }
    
    /**
     * Return the primary table for the given class.
     */
    public Table getTable(final ClassMapping cls, boolean adapt) {
    	return getTable(cls, _tableName, adapt);
    }
    
    /**
     * Return the datastore identity columns for the given class, based on the
     * given templates.
     */
    public Column[] getDataStoreIdColumns(ClassMapping cls, Column[] tmplates,
        Table table, boolean adapt) {
        cls.getMappingRepository().getMappingDefaults().
            populateDataStoreIdColumns(cls, table, tmplates);
        return createColumns(cls, "datastoreid", tmplates, table, adapt);
    }

    /**
     * Return the join from this class to its superclass. The table for
     * this class must be set.
     */
    public ForeignKey getSuperclassJoin(final ClassMapping cls, Table table,
        boolean adapt) {
        ClassMapping sup = cls.getJoinablePCSuperclassMapping();
        if (sup == null)
            return null;

        ForeignKeyDefaults def = new ForeignKeyDefaults() {
            public ForeignKey get(Table local, Table foreign, boolean inverse) {
                return cls.getMappingRepository().getMappingDefaults().
                    getJoinForeignKey(cls, local, foreign);
            }

            public void populate(Table local, Table foreign, Column col,
                Object target, boolean inverse, int pos, int cols) {
                cls.getMappingRepository().getMappingDefaults().
                    populateJoinColumn(cls, local, foreign, col, target,
                        pos, cols);
            }
        };
        return createForeignKey(cls, "superclass", getColumns(), def, table,
            cls, sup, false, adapt);
    }

    /**
     * Synchronize internal information with the mapping data for the given
     * class.
     */
    public void syncWith(ClassMapping cls) {
        clear(false);

        ClassMapping sup = cls.getMappedPCSuperclassMapping();
        if (cls.getTable() != null && (sup == null
            || sup.getTable() != cls.getTable()))
            _tableName = cls.getMappingRepository().getDBDictionary().
                getFullIdentifier(cls.getTable(), true);

        // set io before syncing cols
        setColumnIO(cls.getColumnIO());
        if (cls.getJoinForeignKey() != null && sup != null
            && sup.getTable() != null)
            syncForeignKey(cls, cls.getJoinForeignKey(), cls.getTable(),
                sup.getTable());
        else if (cls.getIdentityType() == ClassMapping.ID_DATASTORE)
            syncColumns(cls, cls.getPrimaryKeyColumns(), false);

        // record inheritance strategy if class does not use default strategy
        // for base classes, and for all subclasses so we can be sure subsequent
        // mapping runs don't think subclass is unmapped
        String strat = (cls.getStrategy() == null) ? null
            : cls.getStrategy().getAlias();
        if (strat != null && (cls.getPCSuperclass() != null
            || !FullClassStrategy.ALIAS.equals(strat)))
            setStrategy(strat);        
    }

    public boolean hasSchemaComponents() {
        return super.hasSchemaComponents() || !DBIdentifier.isNull(_tableName);
    }

    protected void clear(boolean canFlags) {
        super.clear(canFlags);
        _tableName = DBIdentifier.NULL;
    }

    public void copy(MappingInfo info) {
        super.copy(info);
        if (!(info instanceof ClassMappingInfo))
            return;

        ClassMappingInfo cinfo = (ClassMappingInfo) info;
        if (DBIdentifier.isNull(_tableName))
            _tableName = cinfo.getTableIdentifier();
        if (_subStrat == null)
            _subStrat = cinfo.getHierarchyStrategy();
        if (cinfo._seconds != null) {
            if (_seconds == null)
                _seconds = new HashMap<DBIdentifier, List<Column>>();
            DBIdentifier key;
            for (Iterator<DBIdentifier> itr = cinfo._seconds.keySet().iterator();
                itr.hasNext();) {
                key = itr.next();
                if (!_seconds.containsKey(key))
                    _seconds.put(key, cinfo._seconds.get(key));
            }
        }
        if (cinfo._uniques != null) {
        	if (_uniques == null)
        		_uniques = new HashMap<DBIdentifier, List<Unique>>();
        for (Entry<DBIdentifier, List<Unique>> entry : cinfo._uniques.entrySet())
        		if (!_uniques.containsKey(entry.getKey()))
        			_uniques.put(entry.getKey(), entry.getValue());
        }

    }
    
    /**
     * Add a unique constraint for the given table.
     * @param table must be primary table or secondary table name added a 
     * priori to this receiver.
     * @param unique the unique constraint. null means no-op.
     * @deprecated
     */
    public void addUnique(String table, Unique unique) {
        addUnique(DBIdentifier.newTable(table), unique);
    }

    /**
     * Add a unique constraint for the given table.
     * @param table must be primary table or secondary table name added a 
     * priori to this receiver.
     * @param unique the unique constraint. null means no-op.
     */
    public void addUnique(DBIdentifier table, Unique unique) {
    	if (!DBIdentifier.equal(_tableName, table) &&
    	   (_seconds == null || !_seconds.containsKey(table))) {
            throw new UserException(_loc.get("unique-no-table", 
                    new Object[]{table, _className, _tableName, 
                    ((_seconds == null) ? "" : _seconds.keySet())}));
    	}
    	if (unique == null)
    		return;
        if (_uniques == null)
            _uniques = new HashMap<DBIdentifier,List<Unique>>();
        unique.setTableIdentifier(table);
        List<Unique> uniques = _uniques.get(table);
        if (uniques == null) {
        	uniques = new ArrayList<Unique>();
        	uniques.add(unique);
        	_uniques.put(table, uniques);
        } else {
        	uniques.add(unique);
        }
    }
    
    /**
     * Get the unique constraints of the given primary or secondary table.
     * @deprecated
     */
    public Unique[] getUniques(String table) {
        return getUniques(DBIdentifier.newTable(table));
    }

    /**
     * Get the unique constraints of the given primary or secondary table.
     */
    public Unique[] getUniques(DBIdentifier table) {
        if (_uniques == null || _uniques.isEmpty() 
        || _uniques.containsKey(table))
            return new Unique[0];
        List<Unique> uniques = _uniques.get(table);
        return uniques.toArray(new Unique[uniques.size()]);
    }
    
    /**
     * Get all the unique constraints associated with both the primary and/or 
     * secondary tables.
     * 
     */
    public Unique[] getUniques(MetaDataContext cm, boolean adapt) {
        if (_uniques == null || _uniques.isEmpty())
            return new Unique[0];
        List<Unique> result = new ArrayList<Unique>();
        for (DBIdentifier tableName : _uniques.keySet()) {
        	List<Unique> uniqueConstraints = _uniques.get(tableName);
        	for (Unique template : uniqueConstraints) {
        		Column[] templateColumns = template.getColumns();
                Column[] uniqueColumns = new Column[templateColumns.length];
                Table table = getTable((ClassMapping)cm, tableName, adapt);
        		for (int i=0; i<uniqueColumns.length; i++) {
                    DBIdentifier columnName = templateColumns[i].getIdentifier();
        			if (!table.containsColumn(columnName)) {
                        throw new UserException(_loc.get(
                                "unique-missing-column", 
                                new Object[]{cm, columnName, tableName, 
                                Arrays.toString(table.getColumnNames())}));
        			}
                    Column uniqueColumn = table.getColumn(columnName);
        			uniqueColumns[i] = uniqueColumn;
        		}
        		Unique unique = createUnique(cm, "unique", template,  
        				uniqueColumns, adapt);
        		if (unique != null)
        			result.add(unique);
        	}
        }
        return result.toArray(new Unique[result.size()]);
    }   
    
    public File getSourceFile() {
        return _file;
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File file, int srcType) {
        _file = file;
        _srcType = srcType;
    }

    public String getResourceName() {
        return _className;
    }

    public String[] getComments() {
        return (_comments == null) ? EMPTY_COMMENTS : _comments;
    }

    public void setComments(String[] comments) {
        _comments = comments;
    }
    
    public int getLineNumber() {
        return _lineNum;
    }

    public void setLineNumber(int lineNum) {
        _lineNum = lineNum;
    }

    public int getColNumber() {
        return _colNum;
    }

    public void setColNumber(int colNum) {
        _colNum = colNum;
    }
}
