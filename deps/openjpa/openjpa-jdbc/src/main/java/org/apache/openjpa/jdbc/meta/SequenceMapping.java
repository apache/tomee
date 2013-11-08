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

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCSeqValue;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq;
import org.apache.openjpa.jdbc.kernel.TableJDBCSeq;
import org.apache.openjpa.jdbc.kernel.ValueTableJDBCSeq;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.meta.SequenceMetaData;

/**
 * Specialization of sequence metadata for ORM.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class SequenceMapping
    extends SequenceMetaData {

    /**
     * {@link ValueTableJDBCSeq} alias.
     */
    public static final String IMPL_VALUE_TABLE = "value-table";

    /**
     * {@link TableJDBCSeq} alias.
     */
    public static final String IMPL_TABLE = "table";

    /**
     * {@link ClassTableJDBCSeq} alias.
     */
    public static final String IMPL_CLASS_TABLE = "class-table";

    // plugin property names for standard props
    private static final String PROP_TABLE = "Table";
    private static final String PROP_SEQUENCE_COL = "SequenceColumn";
    private static final String PROP_PK_COL = "PrimaryKeyColumn";
    private static final String PROP_PK_VALUE = "PrimaryKeyValue";
    private static final String PROP_UNIQUE = "UniqueColumns";
    private static final String PROP_UNIQUE_CONSTRAINT = "UniqueConstraintName";

    private File _mapFile = null;
    private DBIdentifier _table = DBIdentifier.NULL;
    private DBIdentifier _sequenceColumn = DBIdentifier.NULL;
    private DBIdentifier _primaryKeyColumn = DBIdentifier.NULL;
    private String _primaryKeyValue = null;
    private DBIdentifier[] _uniqueColumns   = null;
    private DBIdentifier _uniqueConstraintName = DBIdentifier.NULL;
    
    /**
     * @deprecated
     * @param name
     * @param repos
     */
    public SequenceMapping(String name, MappingRepository repos) {
        super(name, repos);
    }

    /**
     * Sequence names are a kernel object so DBIdentifiers must be converted to
     * strings 
     * @param name
     * @param repos
     */
    public SequenceMapping(DBIdentifier name, MappingRepository repos) {
        super(DBIdentifier.isNull(name) ? null : name.getName(), repos);
    }

    /**
     * Allow sequence to have a mapping file separate from its metadata
     * source file.
     */
    public File getMappingFile() {
        return _mapFile;
    }

    /**
     * Allow sequence to have a mapping file separate from its metadata
     * source file.
     */
    public void setMappingFile(File file) {
        _mapFile = file;
    }

    /**
     * Name of sequence table, if any.
     * @deprecated
     */
    public String getTable() {
        return getTableIdentifier().getName();
    }

    public DBIdentifier getTableIdentifier() {
        return _table == null ? DBIdentifier.NULL : _table ;
    }

    /**
     * Name of sequence table, if any.
     * @deprecated
     */
    public void setTable(String table) {
        setTableIdentifier(DBIdentifier.newTable(table));
    }

    public void setTableIdentifier(DBIdentifier table) {
        _table = table;
    }

    /**
     * Name of sequence column, if any.
     * @deprecated
     */
    public String getSequenceColumn() {
        return getSequenceColumnIdentifier().getName();
    }

    public DBIdentifier getSequenceColumnIdentifier() {
        return _sequenceColumn == null ? DBIdentifier.NULL : _sequenceColumn;
    }

    /**
     * Name of sequence column, if any.
     * @deprecated
     */
    public void setSequenceColumn(String sequenceColumn) {
        setSequenceColumnIdentifier(DBIdentifier.newColumn(sequenceColumn));
    }

    public void setSequenceColumnIdentifier(DBIdentifier sequenceColumn) {
        _sequenceColumn = sequenceColumn;
    }

    /**
     * Name of primary key column, if any.
     * @deprecated
     */
    public String getPrimaryKeyColumn() {
        return getPrimaryKeyColumnIdentifier().getName();
    }

    public DBIdentifier getPrimaryKeyColumnIdentifier() {
        return _primaryKeyColumn == null ? DBIdentifier.NULL : _primaryKeyColumn;
    }

    /**
     * Name of primary key column, if any.
     * @deprecated
     */
    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        setPrimaryKeyColumnIdentifier(DBIdentifier.newColumn(primaryKeyColumn));
    }

    public void setPrimaryKeyColumnIdentifier(DBIdentifier primaryKeyColumn) {
        _primaryKeyColumn = primaryKeyColumn;
    }

    /**
     * Primary key value, if not auto-determined.
     */
    public String getPrimaryKeyValue() {
        return _primaryKeyValue;
    }

    /**
     * Primary key value, if not auto-determined.
     */
    public void setPrimaryKeyValue(String primaryKeyValue) {
        _primaryKeyValue = primaryKeyValue;
    }

    /**
     * @deprecated
     * @param cols
     */
    public void setUniqueColumns(String[] cols) {
        setUniqueColumnsIdentifier(DBIdentifier.toArray(cols, DBIdentifierType.COLUMN));
    }

    public void setUniqueColumnsIdentifier(DBIdentifier[] cols) {
        _uniqueColumns = cols;
    }

    /**
     * @deprecated
     */
    public String[] getUniqueColumns() {
        return DBIdentifier.toStringArray(getUniqueColumnsIdentifier());
    }

    public DBIdentifier[] getUniqueColumnsIdentifier() {
    	return _uniqueColumns;
    }

    
    protected PluginValue newPluginValue(String property) {
        return new JDBCSeqValue(property);
    }

    @Override
    protected void addStandardProperties(StringBuilder props) {
        super.addStandardProperties(props);
        // Quotes are conditionally added to the following because the props
        // are eventually passed to the Configurations.parseProperties()
        // method, which strips off quotes. This is a problem when these
        // properties are intentionally delimited with quotes. So, an extra
        // set preserves the intended ones. While this is an ugly solution,
        // it's less ugly than other ones.
        
        appendProperty(props, PROP_TABLE, addQuotes(_table.getName()));
        appendProperty(props, PROP_SEQUENCE_COL, addQuotes(_sequenceColumn.getName()));
        appendProperty(props, PROP_PK_COL, addQuotes(_primaryKeyColumn.getName()));
        appendProperty(props, PROP_PK_VALUE, addQuotes(_primaryKeyValue));
        // Array of unique column names are passed to configuration
        // as a single string "x|y|z". The configurable (TableJDBCSeq) must
        // parse it back.
        if (!DBIdentifier.isNull(_uniqueConstraintName) && 
                _uniqueConstraintName.getName().length() > 0) {
            appendProperty(props, PROP_UNIQUE_CONSTRAINT, 
                addQuotes(_uniqueConstraintName.getName()));
        }
            
        if (_uniqueColumns != null && _uniqueColumns.length > 0)
        	appendProperty(props, PROP_UNIQUE, 
        			StringUtils.join(_uniqueColumns,'|'));
    }
    
    private String addQuotes(String name) {
        if (name != null && name.contains(IdentifierUtil.DOUBLE_QUOTE)) {
            return IdentifierUtil.DOUBLE_QUOTE + name + IdentifierUtil.DOUBLE_QUOTE;
        }
        return name;
    }

    /**
     * @deprecated
     * @param name
     */
    public void setUniqueConstraintName(String name) {
        _uniqueConstraintName = DBIdentifier.newConstraint(name);
    }

    public void setUniqueConstraintIdentifier(DBIdentifier name) {
        _uniqueConstraintName = name;
    }

    /**
     * @deprecated
     * @return
     */
    public String getUniqueConstraintName() {
        return getUniqueConstraintIdentifier().getName();
    }

    public DBIdentifier getUniqueConstraintIdentifier() {
        return _uniqueConstraintName == null ? DBIdentifier.NULL : _uniqueConstraintName;
        
    }
}
