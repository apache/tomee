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
package org.apache.openjpa.jdbc.identifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.lib.identifier.Identifier;

/**
 * This class extends DBIdentifier to provide support for qualified identifiers
 * such as schema qualified tables and table qualified columns.  It provides methods
 * to create qualified identifiers from individual identifiers.
 *
 */
public class QualifiedDBIdentifier extends DBIdentifier implements Identifier, Cloneable, Serializable {
    
    private DBIdentifier _schemaName = DBIdentifier.NULL;  // The schema name
    // The table name if the object (column, constraint) is qualified by a table name
    private DBIdentifier _objectTableName = DBIdentifier.NULL; 
    
    
    protected QualifiedDBIdentifier(DBIdentifier... sNames) {
        setPath(sNames);
    }
    
    /**
     * Creates a qualified identifier based upon an array of DBIdentifiers.  Identifiers
     * must be specified in order.
     * @param names
     * @return
     */
    public static QualifiedDBIdentifier newPath(DBIdentifier...names) {
        return new QualifiedDBIdentifier(names);
    }
    
    /**
     * Set the identifiers that make up the path.  Identifiers must be specified
     * in path order.  (ex. [ table, column ] )
     * @param sNames
     */
    public void setPath(DBIdentifier...sNames) {
        resetNames();
        if (sNames == null || sNames.length == 0) {
            return;
        }
        
        if (sNames.length == 1) {
            DBIdentifier sName = sNames[0];
            if (sName.getType() == DBIdentifierType.SCHEMA) {
                setSchemaName(sName.clone());
            }
            setName(sName.clone());
            setType(sName.getType());
            return;
        }

        for (int i = (sNames.length - 1); i >= 0; i--) {
            DBIdentifier sName = sNames[i];
            if (DBIdentifier.isNull(sName)) {
                continue;
            }
            if (i == (sNames.length - 1) && sNames.length != 1) {
                setName(sName.clone());
            } else {
                if (sName.getType() == DBIdentifierType.SCHEMA) {
                    setSchemaName(sName.clone());
                }
                else if (sName.getType() == DBIdentifierType.TABLE) {
                    setObjectTableName(sName.clone());
                }
            }
        }   
    }
    
    // Reset the path names
    private void resetNames() {
        _schemaName = DBIdentifier.NULL;
        _objectTableName = DBIdentifier.NULL;
    }

    /**
     * Splits a qualified path into separate identifiers.
     * @param sName
     * @return
     */
    public static DBIdentifier[] splitPath(DBIdentifier sName) {
        if (sName instanceof QualifiedDBIdentifier && sName.getType() != DBIdentifierType.SCHEMA) {
            QualifiedDBIdentifier path = (QualifiedDBIdentifier)sName;
            List<DBIdentifier> names = new ArrayList<DBIdentifier>();
            
            if (!DBIdentifier.isNull(path.getSchemaName())) {
                names.add(path.getSchemaName().clone());
            }
            if (!DBIdentifier.isNull(path.getObjectTableName())) {
                names.add(path.getObjectTableName().clone());
            }
            if (!DBIdentifier.isNull(path.getIdentifier())) {
                names.add(((DBIdentifier)path).clone());
            }
            return names.toArray(new DBIdentifier[names.size()]);
        }
        if (sName instanceof DBIdentifier) {
            return new DBIdentifier[] { sName.clone() };
        }
        return new DBIdentifier[] {};
    }

    /**
     * Creates a qualified path from an identifier.
     * @param sName
     * @return
     */
    public static QualifiedDBIdentifier getPath(DBIdentifier sName) {
        if (sName instanceof QualifiedDBIdentifier) {
            return (QualifiedDBIdentifier)sName.clone();
        }
        return QualifiedDBIdentifier.newPath(sName);
    }

    /**
     *Sets the schema component of the path.
     */
    public void setSchemaName(DBIdentifier schemaName) {
        _schemaName = schemaName;
    }

    /**
     * Gets the schema component of the path.
     * @return
     */
    public DBIdentifier getSchemaName() {
        return _schemaName;
    }

    /**
     * Sets the object table name component of the path, if the path
     * is a table qualified identifier such as a constraint or column.
     */
    public void setObjectTableName(DBIdentifier objectName) {
        _objectTableName = objectName;
    }

    
    /**
     * Gets the object table name component of the path, if the path
     * is a table qualified identifier such as a constraint or column.
     */
    public DBIdentifier getObjectTableName() {
        return _objectTableName;
    }

    /**
     * Returns true if this object is not qualified by a schema component.
     * @return
     */
    public boolean isUnqualifiedObject() {
        return DBIdentifier.isNull(getSchemaName());
    }

    /**
     * Returns true if this object is not qualified by a table or schema 
     * component.
     * @return
     */
    public boolean isUnqualifiedColumn() {
        return DBIdentifier.isNull(getObjectTableName()) && DBIdentifier.isNull(getSchemaName());
    }

    /**
     * Equality operator.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof QualifiedDBIdentifier) {
            QualifiedDBIdentifier sPath = (QualifiedDBIdentifier)obj;
            return DBIdentifier.equal(sPath.getSchemaName(), getSchemaName()) &&
                DBIdentifier.equal(sPath.getObjectTableName(), getObjectTableName()) &&
                DBIdentifier.equal(sPath, this);
            
        }
        else if (obj instanceof DBIdentifier) {
            DBIdentifier sName = (DBIdentifier)obj;
            return DBIdentifier.equal(sName, this);
        } else if (obj instanceof String) {
            return obj.equals(this.getName());
        }
        throw new IllegalArgumentException("Cannot compare to type: " + obj.getClass().getName());
    }
    
    /**
     * Compares two qualified identifiers for equality.
     * @param path1
     * @param path2
     * @return
     */
    public static boolean pathEqual(QualifiedDBIdentifier path1, QualifiedDBIdentifier path2) {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null) {
            return false;
        }
        DBIdentifier[] names1 = QualifiedDBIdentifier.splitPath(path1);
        DBIdentifier[] names2 = QualifiedDBIdentifier.splitPath(path2);
        if (names1.length != names2.length) {
            return false;
        }
        for (int i = 0; i < names1.length; i++) {
            if (!DBIdentifier.equal(names1[i], names2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a fully qualified name as a string.
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the fully qualified name as a string
     */
    public String getName() {
        // If no schema or object or table qualifier, return the base name
        if (DBIdentifier.isEmpty(_schemaName) && DBIdentifier.isEmpty(_objectTableName)) {
            return getBaseName();
        }
        DBIdentifier[] names = QualifiedDBIdentifier.splitPath(this);
        return DBIdentifier.join(names);
    }
    
    /**
     * Returns the base name of this qualified name.  For example, if the 
     * qualified name is a schema qualified table, the table name is returned.
     * @return
     */
    public String getBaseName() {
        return super.getName();
    }

    /**
     * Returns this object as a DBIdentifier.
     * @return
     */
    public DBIdentifier getIdentifier() {
        return this;
    }
    
    /**
     * Set the name of this qualified identifier.  Splits the string into
     * multiple components.  This method assumes the type does not change.
     */
    public void setName(String name) {
        // Split apart name into components
        DBIdentifier[] sNames = DBIdentifier.split(getType(), name);
        setPath(sNames);
    }

    /**
     * Set the base name component of this compound identifier
     * @param name
     */
    protected void setBaseName(String name) {
        super.setName(name);
    }

    /**
     * Returns the length of the qualified identifier, including delimiters
     * and name separators.
     */
    public int length() {
        String name = getName();
        if (name == null) {
            return 0;
        }
        return name.length();
    }

    /**
     * Compares this identifier with another identifier.
     */
    public int compareTo(Identifier o) {
        if (o instanceof DBIdentifier) {
            if (o == null || o == DBIdentifier.NULL)
                return -1;
            return super.compareTo(o);
        }
        return getName().compareTo(o.getName());
    }
    
    /**
     * Returns true if all identifiers within this compound identifier are
     * delimited. Otherwise, false.
     */
    @Override
    public boolean isDelimited() {
        if (DBIdentifier.isEmpty(this)) {
            return false;
        }
        if (!DBIdentifier.isNull(getObjectTableName())) {
            if (!Normalizer.isDelimited(getObjectTableName().getName())) {
                return false;
            }
        }
        if (!DBIdentifier.isNull(getSchemaName())) {
            if (!Normalizer.isDelimited(getSchemaName().getName())) {
                return false;
            }
        }
        return super.isDelimited();
    }
    
    /**
     * Returns a new unqualified name based on this instance.
     */
    @Override
    public DBIdentifier getUnqualifiedName() {
        QualifiedDBIdentifier newName = (QualifiedDBIdentifier)clone();
        newName.setObjectTableName(DBIdentifier.NULL);
        newName.setSchemaName(DBIdentifier.NULL);
        return newName;
    }
    
    /**
     * Creates a clone of this identifier.
     */
    public QualifiedDBIdentifier clone() {
        QualifiedDBIdentifier sPath = new QualifiedDBIdentifier();
        sPath.setObjectTableName(getObjectTableName().clone());
        sPath.setSchemaName(getSchemaName().clone());
        sPath.setBaseName(super.getName());
        sPath.setType(getType());
        sPath.setIgnoreCase(getIgnoreCase());
        return sPath;
    }

}
