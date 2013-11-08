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

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;

/**
 * A table constraint. This class is closely aligned with the constraint
 * information available from {@link java.sql.DatabaseMetaData}.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public abstract class Constraint
    extends ReferenceCounter {

    private DBIdentifier _name = DBIdentifier.NULL;
    private QualifiedDBIdentifier _fullPath = null;
    private Table _table = null;
    private DBIdentifier _tableName = DBIdentifier.NULL;
    private DBIdentifier _schemaName = DBIdentifier.NULL;
    private DBIdentifier _columnName = DBIdentifier.NULL;
    private boolean _deferred = false;

    /**
     * Default constructor.
     */
    Constraint() {
    }

    /**
     * Constructor.
     *
     * @param name the name of the constraint, or null if none
     * @param table the local table of the constraint
     * @deprecated
     */
    Constraint(String name, Table table) {
        this(DBIdentifier.newConstant(name), table);
    }

    Constraint(DBIdentifier name, Table table) {
        setIdentifier(name);
        if (table != null) {
            setTableIdentifier(table.getIdentifier());
            setSchemaIdentifier(table.getSchemaIdentifier());
        }
        _table = table;
    }

    /**
     * Called when the constraint is removed from the owning table.
     * Invalidates the constraint.
     */
    void remove() {
        _table = null;
    }

    /**
     * Return the table of this constraint.
     */
    public Table getTable() {
        return _table;
    }

    /**
     * Return the column's table name.
     * @deprecated
     */
    public String getTableName() {
        return getTableIdentifier().getName();
    }

    public DBIdentifier getTableIdentifier() {
        return _tableName == null ? DBIdentifier.NULL : _tableName;
    }

    /**
     * Set the column's table name. You can only call this method on
     * columns whose table object is not set.
     * @deprecated
     */
    public void setTableName(String name) {
        setTableIdentifier(DBIdentifier.newTable(name));
    }

      public void setTableIdentifier(DBIdentifier name) {
          if (getTable() != null)
              throw new IllegalStateException();
          _tableName = name;
          _fullPath = null;
      }

    
    /**
     * Return the column table's schema name.
     * @deprecated
     */
    public String getSchemaName() {
        return getSchemaIdentifier().getName();
    }

    public DBIdentifier getSchemaIdentifier() {
        return _schemaName == null ? DBIdentifier.NULL : _schemaName;
    }

    /**
     * Set the column table's schema name. You can only call this method on
     * columns whose table object is not set.
     * @deprecated
     */
    public void setSchemaName(String schema) {
        setSchemaIdentifier(DBIdentifier.newSchema(schema));
    }

    public void setSchemaIdentifier(DBIdentifier schema) {
        if (getTable() != null)
            throw new IllegalStateException();
        _schemaName = schema;
    }

    /**
     * Return the column's name.
     * @deprecated
     */
    public String getColumnName() {
        return getColumnIdentifier().getName();
    }

    public DBIdentifier getColumnIdentifier() {
        return _columnName == null ? DBIdentifier.NULL : _columnName;
    }

    /**
     * Set the column's name. You can only call this method on
     * columns whose table object is not set.
     * @deprecated
     */
    public void setColumnName(String name) {
        setColumnIdentifier(DBIdentifier.newColumn(name));
    }

    public void setColumnIdentifier(DBIdentifier name) {
        if (getTable() != null)
            throw new IllegalStateException();
        _columnName = name;
    }

    /**
     * Return the name of the constraint.
     * @deprecated
     */
    public String getName() {
        return getIdentifier().getName();
    }
    
    public DBIdentifier getIdentifier() {
        return _name == null ? DBIdentifier.NULL : _name;
    }


    /**
     * Set the name of the constraint. This method cannot be called if the
     * constraint already belongs to a table.
     * @deprecated
     */
    public void setName(String name) {
        setIdentifier(DBIdentifier.newConstraint(name));
    }

    public void setIdentifier(DBIdentifier name) {
        if (getTable() != null)
            throw new IllegalStateException();
        _name = name;
        _fullPath = null;
    }

    /**
     * Return the full name of the constraint.
     * @deprecated
     */
    public String getFullName() {
        return getFullIdentifier().getName();
    }

    public QualifiedDBIdentifier getQualifiedPath() {
        if (_fullPath == null) {
            _fullPath = QualifiedDBIdentifier.newPath(getTableIdentifier(), getIdentifier());
        }
        return _fullPath;
    }

    public DBIdentifier getFullIdentifier() {
        return getQualifiedPath().getIdentifier();
    }
    
    
    /**
     * Return whether this constraint is a logical constraint only; i.e.
     * if it does not exist in the database.
     */
    public abstract boolean isLogical();

    /**
     * Return true if this is a deferred constraint.
     */
    public boolean isDeferred() {
        return _deferred;
    }

    /**
     * Make this constrain deferred.
     */
    public void setDeferred(boolean deferred) {
        _deferred = deferred;
    }

    public String toString() {
        if (!getIdentifier().isNull())
            return getIdentifier().getName();

        String name = getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        return "<" + name.toLowerCase() + ">";
    }
}
