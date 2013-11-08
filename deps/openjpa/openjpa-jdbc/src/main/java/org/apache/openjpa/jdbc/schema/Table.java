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
package org.apache.openjpa.jdbc.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.meta.SourceTracker;

/**
 * Represents a database table.
 *
 * @author Abe White
 * @author Stephen Kim
 */
@SuppressWarnings("serial")
public class Table
    extends NameSet
    implements Comparable<Object>, SourceTracker {

    private DBIdentifier _name = DBIdentifier.NULL;
    private DBIdentifier _schemaName = DBIdentifier.NULL;
    private Map<DBIdentifier, Column> _colMap = null;
    private Map<DBIdentifier, Index> _idxMap = null;
    private Collection<ForeignKey> _fkList = null;
    private Collection<Unique> _unqList = null;
    private Schema _schema = null;
    private PrimaryKey _pk = null;

    // keep track of source
    private File _source = null;
    private int _srcType = SRC_OTHER;

    // cache
    private Column[] _cols = null;
    private Column[] _autoAssign = null;
    private Column[] _rels = null;
    private ForeignKey[] _fks = null;
    private Index[] _idxs = null;
    private Unique[] _unqs = null;
    private String _comment = null;
    private int _lineNum = 0;  
    private int _colNum = 0;
    private boolean _isAssociation = false;
    private QualifiedDBIdentifier _fullPath = null;

    /**
     * Default constructor.
     */
    public Table() {
    }

    /**
     * Constructor.
     *
     * @param name the table name
     * @param schema the table schema
     * @deprecated
     */
    public Table(String name, Schema schema) {
        this(DBIdentifier.newTable(name), schema);
    }

    public Table(DBIdentifier name, Schema schema) {
        setIdentifier(name);
        addName(name, true);
        if (schema != null)
            setSchemaIdentifier(schema.getIdentifier());
        _schema = schema;
    }

    public void setAssociation() {
        _isAssociation = true;
    }

    public boolean isAssociation() {
        return _isAssociation;
    }

    /**
     * Called when the table is removed from its schema. Removes all table
     * members, and invalidates the table.
     */
    void remove() {
        ForeignKey[] fks = getForeignKeys();
        for (int i = 0; i < fks.length; i++)
            removeForeignKey(fks[i]);
        Index[] idxs = getIndexes();
        for (int i = 0; i < idxs.length; i++)
            removeIndex(idxs[i]);
        Unique[] unqs = getUniques();
        for (int i = 0; i < unqs.length; i++)
            removeUnique(unqs[i]);
        removePrimaryKey();
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++)
            removeColumn(cols[i]);
        _schema = null;
        _schemaName = DBIdentifier.NULL;
        _fullPath = null;
    }

    /**
     * Return the schema for the table.
     */
    public Schema getSchema() {
        return _schema;
    }

    /**
     * The table's schema name.
     * @deprecated
     */
    public String getSchemaName() {
        return getSchemaIdentifier().getName();
    }

    public DBIdentifier getSchemaIdentifier() {
        return _schemaName == null ? DBIdentifier.NULL : _schemaName;
    }

    /**
     * The table's schema name. You can only call this method on tables
     * whose schema object is not set.
     * @deprecated
     */
    public void setSchemaName(String name) {
        setSchemaIdentifier(DBIdentifier.newSchema(name));
    }

    public void setSchemaIdentifier(DBIdentifier name) {
        if (getSchema() != null)
            throw new IllegalStateException();
        _schemaName = name;
        _fullPath = null;
    }

    /**
     * Return the name of the table.
     * @deprecated
     */
    public String getName() {
        return getIdentifier().getName();
    }
    
    public DBIdentifier getIdentifier() {
        return _name == null ? DBIdentifier.NULL : _name;
    }

    /**
     * Set the name of the table. This method can only be called on tables
     * that are not part of a schema.
     * @deprecated use {@link #setIdentifier(DBIdentifier)} instead.
     */
    @Deprecated
    public void setName(String name) {
        setIdentifier(DBIdentifier.newTable(name));
    }

    /**
     * Set the name of the table. This method can only be called on tables which are not part of a schema. 
     * @param name The name of the table. 
     * @throws IllegalStateException if {@link #getSchema()} does not return null. 
     */
    public void setIdentifier(DBIdentifier name) {
        if (getSchema() != null)
            throw new IllegalStateException();
        _name = name;
        _fullPath = null;
    }

    /**
     * Return the table name, including schema, using '.' as the
     * catalog separator.
     * @deprecated
     */
    public String getFullName() {
        return getFullIdentifier().getName();
    }
    
    public QualifiedDBIdentifier getQualifiedPath() {
        if (_fullPath  == null) {
            _fullPath = QualifiedDBIdentifier.newPath(_schemaName, _name );
        }
        return _fullPath;
    }
    
    public DBIdentifier getFullIdentifier() {
        return getQualifiedPath().getIdentifier();
    }

    public File getSourceFile() {
        return _source;
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File source, int srcType) {
        _source = source;
        _srcType = srcType;
    }

    public String getResourceName() {
        return getFullIdentifier().getName();
    }

    /**
     * Return the table's columns, in alphabetical order.
     */
    public Column[] getColumns() {
        if (_cols == null) {
            if (_colMap == null)
                _cols = Schemas.EMPTY_COLUMNS;
            else {
                Column[] cols = new Column[_colMap.size()];
                Iterator<Column> itr = _colMap.values().iterator();
                for (int i = 0; itr.hasNext(); i++) {
                    cols[i] = (Column) itr.next();
                    cols[i].setIndex(i);
                }
                _cols = cols;
            }
        }
        return _cols;
    }

    /**
     * Return this table's auto-assigned columns.
     */
    public Column[] getAutoAssignedColumns() {
        if (_autoAssign == null) {
            if (_colMap == null)
                _autoAssign = Schemas.EMPTY_COLUMNS;
            else {
                Collection<Column> autos = null;
                Column[] cols = getColumns();
                for (int i = 0; i < cols.length; i++) {
                    if (cols[i].isAutoAssigned()) {
                        if (autos == null)
                            autos = new ArrayList<Column>(3);
                        autos.add(cols[i]);
                    }
                }
                _autoAssign = (autos == null) ? Schemas.EMPTY_COLUMNS
                    : (Column[]) autos.toArray(new Column[autos.size()]);
            }
        }
        return _autoAssign;
    }

    /**
     * Return this table's relation id columns.
     */
    public Column[] getRelationIdColumns() {
        if (_rels == null) {
            if (_colMap == null)
                _rels = Schemas.EMPTY_COLUMNS;
            else {
                Collection<Column> rels = null;
                Column[] cols = getColumns();
                for (int i = 0; i < cols.length; i++) {
                    if (cols[i].isRelationId()) {
                        if (rels == null)
                            rels = new ArrayList<Column>(3);
                        rels.add(cols[i]);
                    }
                }
                _rels = (rels == null) ? Schemas.EMPTY_COLUMNS
                    : (Column[]) rels.toArray(new Column[rels.size()]);
            }
        }
        return _rels;
    }

    public String[] getColumnNames() {
        if (_colMap == null) {
            return new String[0];
        }
        DBIdentifier[] sNames = (DBIdentifier[])_colMap.keySet().toArray(new DBIdentifier[_colMap.size()]); 
        return DBIdentifier.toStringArray(sNames);
    }
    
    /**
     * Return the column with the given name, or null if none.
     * @deprecated
     */
    public Column getColumn(String name) {
        return getColumn(DBIdentifier.newIdentifier(name, DBIdentifierType.COLUMN, true));
    }

    public Column getColumn(DBIdentifier name) {
        if (DBIdentifier.isNull(name) || _colMap == null)
            return null;
        return _colMap.get(DBIdentifier.toUpper(name));
    }

    public Column getColumn(DBIdentifier name, boolean create) {
        return getColumn(name);
    }
    
    /**
     * Affirms if this table contains the column of the given name without any 
     * side-effect. 
     * @see Table#getColumn(String) can have side-effect of creating a column
     * for dynamic table implementation.
     * @deprecated
     */
    public boolean containsColumn(String name) {
        return containsColumn(DBIdentifier.newColumn(name), null);
    }
    
    public boolean containsColumn(DBIdentifier name) {
        return containsColumn(name, null);
    }

    /**
     * Affirms if this table contains the column of the given name without any 
     * side-effect. 
     * @param dict the current database dictionary or null.
     * @see Table#getColumn(String) can have side-effect of creating a column
     * for dynamic table implementation.
     * @deprecated
     */
    public boolean containsColumn(String name, DBDictionary dict) {
        if (name == null || _colMap == null) {
            return false;
        }
        return containsColumn(DBIdentifier.newIdentifier(name, DBIdentifierType.COLUMN, true));
    }

    public boolean containsColumn(DBIdentifier name, DBDictionary dict) {
        if (DBIdentifier.isNull(name) || _colMap == null) {
            return false;
        }
        DBIdentifier sName = DBIdentifier.toUpper(name);
        return _colMap.containsKey(sName);
    }

    public boolean containsColumn(Column col) {
        DBIdentifier colName = col.getIdentifier();
        if (DBIdentifier.isNull(colName) || _colMap == null) {
            return false;
        }
        DBIdentifier sName = DBIdentifier.toUpper(colName);
        Collection<Column> coll = _colMap.values();
        for (Column column : coll) {
            if (column.getIdentifier().equals(sName))
                return true; 
        }
        
        return false;
    }

    /**
     * Add a column to the table.
     * @deprecated
     */
    public Column addColumn(String name) {
        return addColumn(DBIdentifier.newColumn(name));
    }

    public Column addColumn(DBIdentifier name) {
        addName(name, true);
        Schema schema = getSchema();
        Column col;
        if (schema != null && schema.getSchemaGroup() != null) {
            col = schema.getSchemaGroup().newColumn(name, this);
        } else {
            col = new Column(name, this);
        }
        if (_colMap == null)
            _colMap = new LinkedHashMap<DBIdentifier, Column>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _colMap.put(sName, col);
        _cols = null;
        return col;
    }


    /**
     * Add a column with a shortened (i.e., validated) name to the table
     * @deprecated
     */
    public Column addColumn(String name, String validName) {
        return addColumn(DBIdentifier.newColumn(name), DBIdentifier.newColumn(validName));
    }

    public Column addColumn(DBIdentifier name, DBIdentifier validName) {
        addName(name, true);
        Schema schema = getSchema();
        Column col;
        if (schema != null && schema.getSchemaGroup() != null)
            col = schema.getSchemaGroup().newColumn(validName, this);
        else
            col = new Column(validName, this);
        if (_colMap == null)
            _colMap = new LinkedHashMap<DBIdentifier, Column>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _colMap.put(sName, col);
        _cols = null;
        return col;
    }

    /**
     * Add a name to this NameSet
     * @deprecated
     */
    public void addCorrectedColumnName(String name, boolean validate) {
        addName(DBIdentifier.newColumn(name), validate);
    }

    public void addCorrectedColumnName(DBIdentifier name, boolean validate) {
        addName(name, validate);
    }


    /**
     * Remove the given column from the table.
     *
     * @return true if the column was removed, false if not in the table
     */
    public boolean removeColumn(Column col) {
        if (col == null || _colMap == null)
            return false;

        DBIdentifier sName = DBIdentifier.toUpper(col.getIdentifier());
        Column cur = (Column) _colMap.get(sName);
        if (!col.equals(cur))
            return false;

        removeName(sName);
        _colMap.remove(sName);
        _cols = null;
        if (col.isAutoAssigned())
            _autoAssign = null;
        if (col.isRelationId())
            _rels = null;
        col.remove();
        return true;
    }

    /**
     * Import a column from another table.
     */
    public Column importColumn(Column col) {
        if (col == null)
            return null;

        Column copy = addColumn(col.getIdentifier());
        copy.setType(col.getType());
        copy.setTypeIdentifier(col.getTypeIdentifier());
        copy.setJavaType(col.getJavaType());
        copy.setNotNull(col.isNotNull());
        copy.setDefaultString(col.getDefaultString());
        copy.setSize(col.getSize());
        copy.setDecimalDigits(col.getDecimalDigits());
        copy.setAutoAssigned(col.isAutoAssigned());
        copy.setXML(col.isXML());
        return copy;
    }

    /**
     * Return the primary key for the table, if any.
     */
    public PrimaryKey getPrimaryKey() {
        return _pk;
    }

    /**
     * Set the primary key for the table.
     */
    public PrimaryKey addPrimaryKey() {
        return addPrimaryKey(DBIdentifier.NULL);
    }

    /**
     * Set the primary key for the table.
     * @deprecated
     */
    public PrimaryKey addPrimaryKey(String name) {
        return addPrimaryKey(DBIdentifier.newConstraint(name));
    }

    public PrimaryKey addPrimaryKey(DBIdentifier name) {
        Schema schema = getSchema();
        if (schema != null && schema.getSchemaGroup() != null) {
            schema.getSchemaGroup().addName(name, false);
            _pk = schema.getSchemaGroup().newPrimaryKey(name, this);
        } else
            _pk = new PrimaryKey(name, this);
        return _pk;
    }

    /**
     * Remove the primary key from this table.
     *
     * @return true if there was a pk to remove, false otherwise
     */
    public boolean removePrimaryKey() {
        boolean rem = _pk != null;
        if (rem) {
            Schema schema = getSchema();
            if (schema != null && schema.getSchemaGroup() != null)
                schema.getSchemaGroup().removeName(_pk.getIdentifier());
            _pk.remove();
        }
        _pk = null;
        return rem;
    }

    /**
     * Import a primary key; column names must match columns of this table.
     */
    public PrimaryKey importPrimaryKey(PrimaryKey pk) {
        if (pk == null)
            return null;

        PrimaryKey copy = addPrimaryKey(pk.getIdentifier());
        copy.setLogical(pk.isLogical());
        Column[] cols = pk.getColumns();
        for (int i = 0; i < cols.length; i++)
            copy.addColumn(getColumn(cols[i].getIdentifier()));
        return copy;
    }

    /**
     * Return the foreign key with the given name. If multiple foreign keys
     * have the name, the first match is returned.
     * @deprecated
     */
    public ForeignKey getForeignKey(String name) {
        return getForeignKey(DBIdentifier.newForeignKey(name));
    }

    public ForeignKey getForeignKey(DBIdentifier name) {
        ForeignKey[] fks = getForeignKeys();
        for (int i = 0; i < fks.length; i++) {
            if (name.equals(fks[i].getIdentifier())) {
                return fks[i];
            }
        }
        return null;
    }

    /**
     * Return all foreign keys for the table.
     */
    public ForeignKey[] getForeignKeys() {
        if (_fks == null) {
            if (_fkList == null)
                _fks = Schemas.EMPTY_FOREIGN_KEYS;
            else {
                ForeignKey[] fks = new ForeignKey[_fkList.size()];
                Iterator<ForeignKey> itr = _fkList.iterator();
                for (int i = 0; itr.hasNext(); i++) {
                    fks[i] = (ForeignKey) itr.next();
                    fks[i].setIndex(i);
                }
                _fks = fks;
            }
        }
        return _fks;
    }

    /**
     * Add a foreign key to the table.
     */
    public ForeignKey addForeignKey() {
        return addForeignKey(DBIdentifier.NULL);
    }

    /**
     * Add a foreign key to the table. Duplicate key names are not allowed.
     * @deprecated
     */
    public ForeignKey addForeignKey(String name) {
        return addForeignKey(DBIdentifier.newForeignKey(name));
    }

    public ForeignKey addForeignKey(DBIdentifier name) {
        Schema schema = getSchema();
        ForeignKey fk;
        if (schema != null && schema.getSchemaGroup() != null) {
            schema.getSchemaGroup().addName(name, false);
            fk = schema.getSchemaGroup().newForeignKey(name, this);
        } else
            fk = new ForeignKey(name, this);
        if (_fkList == null)
            _fkList = new ArrayList<ForeignKey>(3);
        _fkList.add(fk);
        _fks = null;
        return fk;
    }

    /**
     * Remove the given foreign key from the table.
     *
     * @return true if the key was removed, false if not in the table
     */
    public boolean removeForeignKey(ForeignKey fk) {
        if (fk == null || _fkList == null)
            return false;

        if (!_fkList.remove(fk))
            return false;

        Schema schema = getSchema();
        if (schema != null && schema.getSchemaGroup() != null)
            schema.getSchemaGroup().removeName(fk.getIdentifier());
        _fks = null;
        fk.remove();
        return true;
    }

    /**
     * Import a foreign key; column names must match columns of this table.
     */
    public ForeignKey importForeignKey(ForeignKey fk) {
        if (fk == null)
            return null;

        ForeignKey copy = addForeignKey(fk.getIdentifier());
        copy.setDeleteAction(fk.getDeleteAction());

        Schema schema = getSchema();
        if (schema != null && schema.getSchemaGroup() != null) {
            Column[] pks = fk.getPrimaryKeyColumns();
            Table joined = null;
            if (pks.length > 0)
                joined = schema.getSchemaGroup().findTable(pks[0].getTable());

            Column[] cols = fk.getColumns();
            for (int i = 0; i < cols.length; i++)
                copy.join(getColumn(cols[i].getIdentifier()),
                    joined.getColumn(pks[i].getIdentifier()));

            cols = fk.getConstantColumns();
            for (int i = 0; i < cols.length; i++)
                copy.joinConstant(getColumn(cols[i].getIdentifier()),
                    fk.getPrimaryKeyConstant(cols[i]));

            pks = fk.getConstantPrimaryKeyColumns();
            if (joined == null && pks.length > 0)
                joined = schema.getSchemaGroup().findTable(pks[0].getTable());
            for (int i = 0; i < pks.length; i++)
                copy.joinConstant(fk.getConstant(pks[i]),
                    joined.getColumn(pks[i].getIdentifier()));
        }
        return copy;
    }

    /**
     * Return the table's indexes.
     */
    public Index[] getIndexes() {
        if (_idxs == null || _idxs.length == 0)
            _idxs = (_idxMap == null) ? Schemas.EMPTY_INDEXES : (Index[])
                _idxMap.values().toArray(new Index[_idxMap.size()]);
        return _idxs;
    }

    /**
     * Return the index with the given name, or null if none.
     * @deprecated
     */
    public Index getIndex(String name) {
        if (name == null || _idxMap == null)
            return null;
        return getIndex(DBIdentifier.newIdentifier(name, DBIdentifierType.INDEX, true));
    }

    public Index getIndex(DBIdentifier name) {
        if (name == null || _idxMap == null)
            return null;
        
        DBIdentifier sName = DBIdentifier.toUpper(name);
        return (Index) _idxMap.get(sName);
    }

    /**
     * Add an index to the table.
     * @deprecated
     */
    public Index addIndex(String name) {
        return addIndex(DBIdentifier.newIndex(name));
    }

    public Index addIndex(DBIdentifier name) {
        Schema schema = getSchema();
        Index idx;
        if (schema != null && schema.getSchemaGroup() != null) {
            schema.getSchemaGroup().addName(name, true);
            idx = schema.getSchemaGroup().newIndex(name, this);
        } else
            idx = new Index(name, this);
        if (_idxMap == null)
            _idxMap = new TreeMap<DBIdentifier, Index>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _idxMap.put(sName, idx);
        _idxs = null;
        return idx;
    }

    /**
     * Remove the given index from the table.
     *
     * @return true if the index was removed, false if not in the table
     */
    public boolean removeIndex(Index idx) {
        if (idx == null || _idxMap == null)
            return false;

        DBIdentifier sName = DBIdentifier.toUpper(idx.getIdentifier());
        Index cur = (Index) _idxMap.get(sName);
        if (!idx.equals(cur))
            return false;

        _idxMap.remove(sName);
        Schema schema = getSchema();
        if (schema != null && schema.getSchemaGroup() != null)
            schema.getSchemaGroup().removeName(idx.getIdentifier());
        idx.remove();
        _idxs = null;
        return true;
    }

    /**
     * Import an index; column names must match columns of this table.
     */
    public Index importIndex(Index idx) {
        if (idx == null)
            return null;

        Index copy = addIndex(idx.getIdentifier());
        copy.setUnique(idx.isUnique());

        Column[] cols = idx.getColumns();
        for (int i = 0; i < cols.length; i++)
            copy.addColumn(getColumn(cols[i].getIdentifier()));
        return copy;
    }

    /**
     * Return the table's unique constraints.
     */
    public Unique[] getUniques() {
        if (_unqs == null)
            _unqs = (_unqList == null) ? Schemas.EMPTY_UNIQUES : (Unique[])
                _unqList.toArray(new Unique[_unqList.size()]);
        return _unqs;
    }

    /**
     * Return the unique constraint with the given name, or null if none.
     * @deprecated
     */
    public Unique getUnique(String name) {
        return getUnique(DBIdentifier.newConstraint(name));
    }

    public Unique getUnique(DBIdentifier name) {
        Unique[] unqs = getUniques();
        for (int i = 0; i < unqs.length; i++)
            if (DBIdentifier.equal(name, unqs[i].getIdentifier()))
                return unqs[i];
        return null;
    }

    /**
     * Add a unique constraint to the table.
     * @deprecated
     */
    public Unique addUnique(String name) {
        return addUnique(DBIdentifier.newConstraint(name));
    }

    public Unique addUnique(DBIdentifier name) {
        Schema schema = getSchema();
        Unique unq;
        if (schema != null && schema.getSchemaGroup() != null) {
            schema.getSchemaGroup().addName(name, false);
            unq = schema.getSchemaGroup().newUnique(name, this);
        } else
            unq = new Unique(name, this);
        if (_unqList == null)
            _unqList = new ArrayList<Unique>(3);
        _unqList.add(unq);
        _unqs = null;
        return unq;
    }

    /**
     * Remove the given unique constraint from the table.
     *
     * @return true if the constraint was removed, false if not in the table
     */
    public boolean removeUnique(Unique unq) {
        if (unq == null || _unqList == null)
            return false;

        if (!_unqList.remove(unq))
            return false;

        Schema schema = getSchema();
        if (schema != null && schema.getSchemaGroup() != null)
            schema.getSchemaGroup().removeName(unq.getIdentifier());
        _unqs = null;
        unq.remove();
        return true;
    }

    /**
     * Import a constraint; column names must match columns of this table.
     */
    public Unique importUnique(Unique unq) {
        if (unq == null)
            return null;

        Unique copy = addUnique(unq.getIdentifier());
        copy.setDeferred(unq.isDeferred());

        Column[] cols = unq.getColumns();
        for (int i = 0; i < cols.length; i++)
            copy.addColumn(getColumn(cols[i].getIdentifier()));
        return copy;
    }

    /**
     * Called by columns to ensure that all columns are properly indexed
     * before returning that information to the user.
     */
    void indexColumns() {
        getColumns();
    }

    /**
     * Called by foreign keys to ensure that all fks are properly indexed
     * before returning that information to the user.
     */
    void indexForeignKeys() {
        getForeignKeys();
    }

    /**
     * Called by columns when their auto increment status changes.
     */
    void changeAutoAssigned(Column col) {
        _autoAssign = null;
    }

    /**
     * Called by columns when their relation id status changes.
     */
    void changeRelationId(Column col) {
        _rels = null;
    }

    public int compareTo(Object other) {
        DBIdentifier name = getFullIdentifier();
        DBIdentifier otherName = ((Table) other).getFullIdentifier();
        if (DBIdentifier.isNull(name) && DBIdentifier.isNull(otherName))
            return 0;
        if (DBIdentifier.isNull(name))
            return 1;
        if (DBIdentifier.isNull(otherName))
            return -1;
        return name.compareTo(otherName);
    }

    public String toString() {
        return getFullIdentifier().getName();
    }

    public boolean hasComment() {
        return _comment != null && !_comment.equalsIgnoreCase(_name.getName());
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
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
