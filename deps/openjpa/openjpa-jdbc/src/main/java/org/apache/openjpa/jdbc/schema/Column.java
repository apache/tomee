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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.VersionStrategy;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Represents a database column. Closely aligned with the column
 * information available from {@link DatabaseMetaData}.
 *
 * @author Abe White
 * @author Stephen Kim
 */
@SuppressWarnings("serial")
public class Column
    extends ReferenceCounter {

    public static final int FLAG_UNINSERTABLE = 2 << 0;
    public static final int FLAG_UNUPDATABLE = 2 << 1;
    public static final int FLAG_DIRECT_INSERT = 2 << 2;
    public static final int FLAG_DIRECT_UPDATE = 2 << 3;
    public static final int FLAG_FK_INSERT = 2 << 4;
    public static final int FLAG_FK_UPDATE = 2 << 5;
    public static final int FLAG_PK_JOIN = 2 << 6;

    private DBIdentifier _name = DBIdentifier.NULL;
    private Table _table = null;
    private DBIdentifier _tableName = DBIdentifier.NULL;
    private DBIdentifier _schemaName = DBIdentifier.NULL;
    private int _type = Types.OTHER;
    private DBIdentifier _typeName = DBIdentifier.NULL;
    private int _javaType = JavaTypes.OBJECT;
    private int _size = 0;
    private int _decimals = 0;
    private String _defaultStr = null;
    private Object _default = null;
    private Boolean _notNull = null;
    private boolean _autoAssign = false;
    private boolean _rel = false;
    private boolean _implicitRelation = false;
    private DBIdentifier _target = DBIdentifier.NULL;
    private String _targetField = null;
    private int _flags = 0;
    private QualifiedDBIdentifier _fullPath = null;

    private int _index = 0;
    private boolean _pk = false;
    private VersionStrategy _versionStrategy = null;
    private String _comment = null;
    private boolean _XML = false;
    private boolean _isUni1MFK = false;
    private Set<Constraint> _constraints = new HashSet<Constraint>();
    
    /**
     * Default constructor.
     */
    public Column() {
    }

    /**
     * Constructor.
     *
     * @param name the name of the column
     * @param table the column's table
     * @deprecated
     */
    public Column(String name, Table table) {
        this(DBIdentifier.newColumn(name), table);
    }

    public Column(DBIdentifier name, Table table) {
        setIdentifier(name);
        if (table != null) {
            setTableIdentifier(table.getIdentifier());
            setSchemaIdentifier(table.getSchemaIdentifier());
        }
        _table = table;
    }

    /**
     * Called when the column is removed from its table. Removes the column
     * from all table constraints and indexes, then invalidates it.
     */
    void remove() {
        Table table = getTable();
        if (table == null)
            return;

        Schema schema = table.getSchema();
        if (schema != null && schema.getSchemaGroup() != null) {
            Schema[] schemas = schema.getSchemaGroup().getSchemas();
            Table[] tabs;
            ForeignKey[] fks;
            Column[] cols;
            Column[] pks;
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++) {
                    fks = tabs[j].getForeignKeys();
                    for (int k = 0; k < fks.length; k++) {
                        cols = fks[k].getColumns();
                        pks = fks[k].getPrimaryKeyColumns();
                        for (int l = 0; l < cols.length; l++)
                            if (this.equals(cols[l]) || this.equals(pks[l]))
                                fks[k].removeJoin(cols[l]);

                        cols = fks[k].getConstantColumns();
                        for (int l = 0; l < cols.length; l++)
                            if (this.equals(cols[l]))
                                fks[k].removeJoin(cols[l]);

                        pks = fks[k].getConstantPrimaryKeyColumns();
                        for (int l = 0; l < pks.length; l++)
                            if (this.equals(pks[l]))
                                fks[k].removeJoin(pks[l]);

                        if (fks[k].getColumns().length == 0
                            && fks[k].getConstantColumns().length == 0)
                            tabs[j].removeForeignKey(fks[k]);
                    }
                }
            }
        }

        Index[] idxs = table.getIndexes();
        for (int i = 0; i < idxs.length; i++)
            if (idxs[i].removeColumn(this) && idxs[i].getColumns().length == 0)
                table.removeIndex(idxs[i]);

        Unique[] unqs = table.getUniques();
        for (int i = 0; i < unqs.length; i++)
            if (unqs[i].removeColumn(this) && unqs[i].getColumns().length == 0)
                table.removeUnique(unqs[i]);

        PrimaryKey pk = table.getPrimaryKey();
        if (pk != null && pk.removeColumn(this) && pk.getColumns().length == 0)
            table.removePrimaryKey();

        _table = null;
    }

    /**
     * Return the table for the column.
     */
    public Table getTable() {
        return _table;
    }

    /**
     * The column's table name.
     * @deprecated
     */
    public String getTableName() {
        return getTableIdentifier().getName();
    }

    public DBIdentifier getTableIdentifier() {
        return _tableName == null ? DBIdentifier.NULL : _tableName;
    }

    /**
     * The column's table name. You can only call this method on columns
     * whose table object is not set.
     * @deprecated
     */
    public void setTableName(String name) {
        setTableIdentifier(DBIdentifier.newTable(name));
    }

    public void setTableIdentifier(DBIdentifier name) {
      if (getTable() != null)
          throw new IllegalStateException();
      _tableName = name == null ? DBIdentifier.NULL : name;
      _fullPath = null;
    }

    /**
     * Reset the table name with the fully qualified table name which
     * includes the schema name
     * @deprecated
     */
    public void resetTableName(String name) {
        _tableName = DBIdentifier.newTable(name);
    }
    
    public void resetTableIdentifier(DBIdentifier table) {
        _tableName = table == null ? DBIdentifier.NULL : table;
    }

    /**
     * The column's schema name.
     * @deprecated
     */
    public String getSchemaName() {
        return getSchemaIdentifier().getName();
    }

    public DBIdentifier getSchemaIdentifier() {
        return _schemaName == null ? DBIdentifier.NULL : _schemaName;
    }

    /**
     * The column's schema name. You can only call this method on columns
     * whose table object is not set.
     * @deprecated use setSchemaIdentifier(DBIdentifier name)
     */
    public void setSchemaName(String name) {
        setSchemaIdentifier(DBIdentifier.newSchema(name));
    }

    public void setSchemaIdentifier(DBIdentifier name) {
        if (getTable() != null)
            throw new IllegalStateException();
        _schemaName = name == null ? DBIdentifier.NULL : name;
    }

    /**
     * Return the column's name.
     * @deprecated use getIdentifier()
     */
    public String getName() {
        return getIdentifier().getName();
    }

    public DBIdentifier getIdentifier() {
        return _name == null ? DBIdentifier.NULL : _name;
    }

    
    /**
     * Set the column's name. You can only call this method on columns
     * whose table object is not set.
     * @deprecated use setIdentifier(DBIdentifier name)
     */
    public void setName(String name) {
        setIdentifier(DBIdentifier.newColumn(name));
    }

    public void setIdentifier(DBIdentifier name) {
        if (getTable() != null)
            throw new IllegalStateException();
        _name = name == null ? DBIdentifier.NULL : name;
        _fullPath = null;
    }

    /**
     * Return the column's full name, in the form &lt;table&gt;.&lt;name&gt;.
     * @deprecated use getFullDBIdentifier()
     */
    public String getFullName() {
        return getFullDBIdentifier().getName();
    }

    public DBIdentifier getFullDBIdentifier() {
        return getQualifiedPath().getIdentifier();
    }
    
    public QualifiedDBIdentifier getQualifiedPath() {
        if (_fullPath  == null) {
            _fullPath = QualifiedDBIdentifier.newPath(getTableIdentifier(), getIdentifier() );
        }
        return _fullPath;
    }

    /**
     * Return the column's SQL type. This will be one of the type constants
     * defined in {@link Types}.
     */
    public int getType() {
        return _type;
    }

    /**
     * Set the column's SQL type. This should be one of the type constants
     * defined in {@link Types}.
     */
    public void setType(int sqlType) {
        _type = sqlType;
    }

    /**
     * The database-specific SQL type of this column.
     * @deprecated
     */
    public String getTypeName() {
        return getTypeIdentifier().getName();
    }

    public DBIdentifier getTypeIdentifier() {
        return _typeName == null ? DBIdentifier.NULL : _typeName ;
    }

    /**
     * The database-specific SQL type of this column.
     * @deprecated
     */
    public void setTypeName(String typeName) {
        setTypeIdentifier(DBIdentifier.newColumnDefinition(typeName));
    }

    public void setTypeIdentifier(DBIdentifier typeName) {
        _typeName = typeName == null ? DBIdentifier.NULL : typeName;
    }

    /**
     * The Java type the data in this column is treated as, from
     * {@link JavaTypes} or {@link JavaSQLTypes}.
     */
    public int getJavaType() {
        return _javaType;
    }

    /**
     * The Java type the data in this column is treated as, from
     * {@link JavaTypes} or {@link JavaSQLTypes}.
     */
    public void setJavaType(int type) {
        _javaType = type;
    }

    /**
     * Return the column's size.
     */
    public int getSize() {
        return _size;
    }

    /**
     * Set the column's size.
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     * Return the number of decimal digits for the column, if applicable.
     */
    public int getDecimalDigits() {
        return _decimals;
    }

    /**
     * Set the number of decimal digits for the column.
     */
    public void setDecimalDigits(int digits) {
        _decimals = digits;
    }

    /**
     * Return the default value set for the column, if any.
     */
    public String getDefaultString() {
        return _defaultStr;
    }

    /**
     * Set the default value for the column.
     */
    public void setDefaultString(String def) {
        _defaultStr = def;
        _default = null;
    }

    /**
     * Return the default value set for this column, if any. If only a default
     * string has been set, attempts to convert it to the right type based
     * on the Java type set for this column.
     */
    public Object getDefault() {
        if (_default != null)
            return _default;
        if (_defaultStr == null)
            return null;

        switch (_javaType) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                _default = ("true".equals(_defaultStr)) ? Boolean.TRUE
                    : Boolean.FALSE;
                break;
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                _default = new Byte(_defaultStr);
                break;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                _default = Character.valueOf(_defaultStr.charAt(0));
                break;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                _default = new Double(_defaultStr);
                break;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                _default = new Float(_defaultStr);
                break;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                _default = Integer.parseInt(_defaultStr);
                break;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                _default = Long.parseLong(_defaultStr);
                break;
            case JavaTypes.NUMBER:
            case JavaTypes.BIGDECIMAL:
                _default = new BigDecimal(_defaultStr);
                break;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                _default = new Short(_defaultStr);
                break;
            case JavaTypes.DATE:
                _default = new java.util.Date(_defaultStr);
                break;
            case JavaTypes.BIGINTEGER:
                _default = new BigInteger(_defaultStr);
                break;
            case JavaSQLTypes.SQL_DATE:
                _default = Date.valueOf(_defaultStr);
                break;
            case JavaSQLTypes.TIMESTAMP:
                _default = Timestamp.valueOf(_defaultStr);
                break;
            case JavaSQLTypes.TIME:
                _default = Time.valueOf(_defaultStr);
                break;
            default:
                _default = _defaultStr;
        }
        return _default;
    }

    /**
     * Set the default value for the column.
     */
    public void setDefault(Object def) {
        _default = def;
        _defaultStr = (def == null) ? null : def.toString();
    }

    /**
     * Return true if this is a NOT NULL column.
     */
    public boolean isNotNull() {
        return Boolean.TRUE.equals(_notNull);
    }

    /**
     * Set whether this is a NOT NULL column.
     */
    public void setNotNull(boolean notNull) {
        _notNull = (notNull) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Whether the not-null property has been set.
     */
    public boolean isNotNullExplicit() {
        return _notNull != null;
    }

    /**
     * Whether this column is auto-assigned a value on insert.
     */
    public boolean isAutoAssigned() {
        return _autoAssign;
    }

    /**
     * Whether this column is auto-incrementing.
     */
    public void setAutoAssigned(boolean autoAssign) {
        if (autoAssign != _autoAssign && getTable() != null)
            getTable().changeAutoAssigned(this);
        _autoAssign = autoAssign;
    }

    /**
     * Whether this column stores some form of serialized identity value for
     * a related record. This makes the column dependent on the knowing the
     * final identity of the relation before the column value is set.
     */
    public boolean isRelationId() {
        return _rel;
    }

    /**
     * Whether this column stores some form of serialized identity value for
     * a related record. This makes the column dependent on the knowing the
     * final identity of the relation before the column value is set.
     */
    public void setRelationId(boolean rel) {
        if (rel != _rel && getTable() != null)
            getTable().changeRelationId(this);
        _rel = rel;
    }

    /**
     * The name of the column this column joins to, if any. Used for mapping.
     * @deprecated use getTargetIdentifier()
     */
    public String getTarget() {
        return getTargetIdentifier().getName();
    }

    public DBIdentifier getTargetIdentifier() {
        return _target == null ? DBIdentifier.NULL : _target;
    }

    /**
     * The name of the column this column joins to, if any. Used for mapping.
     * @deprecated use setTargetIdentifier(DBIdentifier target)
     */
    public void setTarget(String target) {
        setTargetIdentifier(DBIdentifier.newColumn(StringUtils.trimToNull(target)));
    }

    public void setTargetIdentifier(DBIdentifier target) {
        _target = target == null ? DBIdentifier.NULL : DBIdentifier.trimToNull(target);
    }

    /**
     * The name of the field this column joins to, if any. Used for mapping.
     */
    public String getTargetField() {
        return _targetField;
    }

    /**
     * The name of the field this column joins to, if any. Used for mapping.
     */
    public void setTargetField(String target) {
        if (target != null && target.length() == 0)
            target = null;
        _targetField = target;
    }

    /**
     * Flags are used for bookkeeping information. They are ignored at runtime.
     */
    public boolean getFlag(int flag) {
        return (_flags & flag) != 0;
    }

    /**
     * Flags are used for bookkeeping information. They are ignored at runtime.
     */
    public void setFlag(int flag, boolean on) {
        if (on)
            _flags |= flag;
        else
            _flags &= ~flag;
    }

    /**
     * Return true if this column belongs to the table's primary key.
     */
    public boolean isPrimaryKey() {
        return _pk;
    }

    /**
     * Set whether this column belongs to the table's primary key.
     */
    void setPrimaryKey(boolean pk) {
        _pk = pk;
    }

    /**
     * Return the column's 0-based index in the owning table.
     */
    public int getIndex() {
        if (getTable() != null)
            getTable().indexColumns();
        return _index;
    }

    /**
     * Set the column's 0-based index in the owning table.
     */
    void setIndex(int index) {
        _index = index;
    }

    /**
     * Whether this column is a LOB.
     */
    public boolean isLob() {
        switch (_type) {
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.CLOB:
                return true;
            default:
                return false;
        }
    }

    /**
     * Return true if this column is compatible with the given JDBC type
     * from {@link Types} and size.
     */
    public boolean isCompatible(int type, String typeName, int size, 
        int decimals) {
        if (type == Types.OTHER || getType() == Types.OTHER)
            return true;

        // note that the given size is currently ignored, but may be useful
        // to dynamically-populating subclasses
        switch (getType()) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.NUMERIC:
            case Types.SMALLINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                switch (type) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.NUMERIC:
                    case Types.SMALLINT:
                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.REAL:
                        return true;
                    default:
                        return false;
                }
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.OTHER:
                switch (type) {
                    case Types.BINARY:
                    case Types.BLOB:
                    case Types.LONGVARBINARY:
                    case Types.VARBINARY:
                    case Types.OTHER:
                        return true;
                    default:
                        return false;
                }
            case Types.CLOB:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
                switch (type) {
                    case Types.CLOB:
                    case Types.CHAR:
                    case Types.LONGVARCHAR:
                    case Types.VARCHAR:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        return true;
                    default:
                        return false;
                }
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                switch (type) {
                    case Types.LONGVARCHAR:
                    case Types.CLOB:
                    case Types.VARCHAR:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        return true;
                    default:
                        return false;
                }
            case 2007:  // Oracle-defined opaque type code for XMLType
                switch (type) {
                    case Types.CHAR:
                    case Types.LONGVARCHAR:
                    case Types.VARCHAR:
                    case Types.CLOB:
                    case Types.BLOB:
                        return true;
                     default:
                         return false;
                }
                
            default:
                return type == getType();
        }
    }

    /**
     * Returns the column name.
     */
    public String toString() {
        return getIdentifier().getName();
    }

    /**
     * Useful for debugging.
     */
    public String getDescription() {
        StringBuilder buf = new StringBuilder();
        buf.append("Full Name: ").append(getFullName()).append("\n");
        buf.append("Type: ").append(Schemas.getJDBCName(getType())).
            append("\n");
        buf.append("Size: ").append(getSize()).append("\n");
        buf.append("Default: ").append(getDefaultString()).append("\n");
        buf.append("Not Null: ").append(isNotNull()).append("\n");
        return buf.toString();
    }

    /**
     * Tests compatibility.
     */
    public boolean equalsColumn(Column col) {
        if (col == this)
            return true;
        if (col == null)
            return false;

        if (!getQualifiedPath().equals(col.getQualifiedPath()))
            return false;
        if (!isCompatible(col.getType(), col.getTypeIdentifier().getName(), col.getSize(),
            col.getDecimalDigits()))
            return false;
        if (getType() == Types.VARCHAR && getSize() > 0 && col.getSize() > 0
            && getSize() != col.getSize())
            return false;
        return true;
    }

    /**
     * Copy information from the given column to this one.
     */
    public void copy(Column from) {
        if (from == null)
            return;
        if (DBIdentifier.isNull(getIdentifier()))
            setIdentifier(from.getIdentifier());
        if (getType() == Types.OTHER)
            setType(from.getType());
        if (DBIdentifier.isNull(getTypeIdentifier()))
            setTypeIdentifier(from.getTypeIdentifier());
        if (getJavaType() == JavaTypes.OBJECT)
            setJavaType(from.getJavaType());
        if (getSize() == 0)
            setSize(from.getSize());
        if (getDecimalDigits() == 0)
            setDecimalDigits(from.getDecimalDigits());
        if (getDefaultString() == null)
            setDefaultString(from.getDefaultString());
        if (!isNotNullExplicit() && from.isNotNullExplicit())
            setNotNull(from.isNotNull());
        if (!isAutoAssigned())
            setAutoAssigned(from.isAutoAssigned());
        if (!isRelationId())
            setRelationId(from.isRelationId());
        if (!isImplicitRelation())
        	setImplicitRelation(from.isRelationId());
        if (DBIdentifier.isNull(getTargetIdentifier()))
            setTargetIdentifier(from.getTargetIdentifier());
        if (getTargetField() == null)
            setTargetField(from.getTargetField());
        if (_flags == 0)
            _flags = from._flags;
        if (!isXML())
            setXML(from.isXML());
        if (!isUni1MFK())
            setUni1MFK(from.isUni1MFK());
        for (Constraint c : _constraints) {
            addConstraint(c);
        }
    }
    
    /**
     * Whether this column is of XML type.
     */
    public boolean isXML() {
        return _XML;
    }

    /**
     * Whether this column is of XML type.
     */
    public void setXML(boolean xml) {
        _XML = xml;
    }

    public VersionStrategy getVersionStrategy() {
        return _versionStrategy;
    }

    public void setVersionStrategy(VersionStrategy strategy) {
        this._versionStrategy = strategy;
    }

    public boolean hasComment() {
        return _comment != null && !_comment.equalsIgnoreCase(_name.toString());
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }
    
    /** 
     *  Affirms if this instance represents an implicit relation. For example, a
     *  relation expressed as the value of primary key of the related class and
	 *  not as object reference.
     *
     * @since 1.3.0
     */
    public boolean isImplicitRelation() {
    	return _implicitRelation;
    }
    
    /**
     * Sets a marker to imply a logical relation that can not have any physical
     * manifest in the database. For example, a relation expressed as the value
     * of primary key of the related class and not as object reference.
     * Populated from @ForeignKey(implicit=true) annotation.
     * The mutator can only transit from false to true but not vice versa.
     * 
     * @since 1.3.0
     */
    public void setImplicitRelation(boolean flag) {
    	_implicitRelation |= flag;
    }
    
    /**
     * Sets a marker to indicate that this instance represents a uni-directional 
     * one to many relation using the foreign key strategy. This non-default 
     * mapping of uni-directional one-to-many is supported in JPA 2.0.  
     * 
     * @since 2.0
     */
    public boolean isUni1MFK() {
        return _isUni1MFK;
    }
    
    /** 
     *  Affirms if this instance represents a uni-directional one to many relation
     *  using the foreign key strategy. This non-default mapping of uni-directional 
     *  one-to-many is supported in JPA 2.0.  
     *
     * @since 2.0
     */
    public void setUni1MFK(boolean isUni1MFK) {
        _isUni1MFK = isUni1MFK;
    }
    
    /**
     * Adds the given constraint to this column.
     */
    public void addConstraint(Constraint c) {
        _constraints.add(c);
    }
    
    /**
     * Removes the given constraint from this column.
     */
    public void removeConstraint(Constraint c) {
        _constraints.remove(c);
    }
    
    /**
     * Affirms if this column has any constraint of given type.
     */
    public boolean hasConstraint(Class<? extends Constraint> type) {
        return !getConstraints(type).isEmpty();
    }
    
    /**
     * Gets all constrains attached this column.
     */
    public Set<Constraint> getConstraints() {
        return _constraints;
    }
    
    /**
     * Gets all constrains of the given type attached to this column.
     */
    public <T extends Constraint> Set<T> getConstraints(Class<T> type) {
        Set<T> result = new HashSet<T>();
        for (Constraint c : _constraints) {
            if (c.getClass() == type) {
                result.add((T)c);
            }
        }
        return result;
    }
    
    /**
     * Affirms if any unique constraint is attached to this column.
     */
    public boolean isUniqueConstraint() {
        return hasConstraint(Unique.class);
    }
    
    /**
     * Affirms if any index constraint is attached to this column.
     */
    public boolean isIndex() {
        return hasConstraint(Index.class);
    }
    
    /**
     * Affirms if any foreign key constraint is attached to this column.
     */
    public boolean isForeignKey() {
        return hasConstraint(ForeignKey.class);
    }  
}
