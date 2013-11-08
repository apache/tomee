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

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.identifier.Identifier;
import org.apache.openjpa.lib.identifier.IdentifierImpl;

/**
 * Encapsulates a database identifier.  With a few exceptions, this class is 
 * intended to treated as immutable.  
 */
public class DBIdentifier extends IdentifierImpl implements Cloneable, Identifier, Serializable {

    /**
     * Database identifier types.
     */
    public enum DBIdentifierType {
        DEFAULT,
        TABLE,
        SCHEMA,
        CATALOG,
        DATABASE,
        COLUMN,
        COLUMN_DEFINITION,
        SEQUENCE,
        CONSTRAINT,
        INDEX,
        FOREIGN_KEY,
        CONSTANT,
        NULL
    }
    
    // Array for quick compound identifier determination.  Compound identifiers
    // can have multi-part names, such as {schema, table} and should be stored
    // as a QualifiedDBIdentifier.
    private static boolean _compoundIdentifier[] = new boolean[DBIdentifierType.values().length];
    
    static {
        _compoundIdentifier[DBIdentifierType.TABLE.ordinal()] = true;
        _compoundIdentifier[DBIdentifierType.COLUMN.ordinal()] = true;
        _compoundIdentifier[DBIdentifierType.SEQUENCE.ordinal()] = true;
        _compoundIdentifier[DBIdentifierType.CONSTRAINT.ordinal()] = true;
        _compoundIdentifier[DBIdentifierType.INDEX.ordinal()] = true;
    }
    
    private DBIdentifierType _type = DBIdentifierType.DEFAULT;
    
    /**
     * Special NULL indicator for db identifiers.
     */
    public static final DBIdentifier NULL = new DBIdentifier(DBIdentifierType.NULL);
    
    public boolean _ignoreCase = false;
    
    // All constructors are protected or private.  Static factory operations
    // should be used to construct new identifiers.
    protected DBIdentifier() {
    }

    /**
     * Returns whether case is ignored during equality operations.
     * @return
     */
    public boolean getIgnoreCase() {
        return _ignoreCase;
    }
    
    public void setIgnoreCase(boolean ignoreCase) {
        _ignoreCase = ignoreCase;
    }

    private DBIdentifier(DBIdentifierType type) {
        setType(type);
    }

    protected DBIdentifier(String name, DBIdentifierType type) {
        setType(type);
        setName(name);
    }

    protected DBIdentifier(String name, DBIdentifierType type, boolean delimit) {
        setType(type);
        setName(name, delimit);
    }
    
    protected DBIdentifier(String name, boolean delimit) {
        setName(name, delimit);
    }


    /**
     * Set the name of the identifier.
     */
    public void setName(String name) {
        setName(name, false);
    }

    /**
     * Set the name of this identifier based upon a given identifier.
     */
    public void setName(DBIdentifier name) {
        assertNotNull();
        if (DBIdentifier.isNull(name)) {
            setNameInternal(null);
            setType(name.getType());
            return;
        }
        setNameInternal(name.getNameInternal());
        setType(name.getType());
    }

    /**
     * Set the name of the identifier and optionally force delimiting of the identifier. 
     */
    public void setName(String name, boolean delimit) {
        assertNotNull();
        
        // Normalize the name, if necessary.  Do not normalize constants or column definitions.
        if (DBIdentifierType.CONSTANT != getType() && DBIdentifierType.COLUMN_DEFINITION != getType()) {
            if (delimit) {
                name = Normalizer.delimit(name, true);
             } else {
                 name = Normalizer.normalizeString(name);
             }
        }
        super.setName(name);
    }
    
    /**
     * Set the type of the identifier
     * @param type
     */
    protected void setType(DBIdentifierType type) {
        _type = type;
    }

    /**
     * Get the identifier type
     * @return the identifier type
     */
    public DBIdentifierType getType() {
        return _type;
    }
    

    /**
     * Splits a string delimited by the specified delimiter of a given name type
     * into an array of DBIdentifier objects.
     * Example:  COL1|"COL 2"|COL3  delim=| --> DBIdentifier[]{ COL1, "COL 2", COL3 }
     * @param name
     * @param id
     * @param delim
     * @return
     */
    public static DBIdentifier[] split(String name, DBIdentifierType id, String delim) {
        
        if (!Normalizer.canSplit(name, delim)) {
            return new DBIdentifier[] { new DBIdentifier(name, id) };
        }
        
        String[] names = Normalizer.splitName(name, delim);
        if (names.length == 0) {
            return new DBIdentifier[] { };
        }
        DBIdentifier[] sNames = new DBIdentifier[names.length];
        for (int i = 0; i < names.length ; i++) {
            sNames[i] = new DBIdentifier(names[i], id);
        }
        return sNames;
    }
    
    /**
     * Joins the list of identifiers using the appropriate delimiters and
     * returns a string based identifier.
     * @param resultId
     * @param names
     * @return
     */
    public static String join(DBIdentifier...names) {
        if (names == null || names.length == 0) {
            return null;
        }
        String[] strNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            strNames[i] = names[i].getNameInternal();
        }
        return Normalizer.joinNames(strNames);
    }
    /**
     * Splits a given DBIdentifier into multiple DBIdentifiers.  Uses the base name 
     * type and heuristics to determine the types and placement of the resulting
     * components.
     * @param name
     * @return
     */
    public static DBIdentifier[] split(DBIdentifierType resultType, String name) {
        
        String[] names = Normalizer.splitName(name);
        switch (names.length) {
            case 2:
                return getTwoPartIdentifier(names, resultType);
            case 3:
                return getThreePartIdentifier(names, resultType);
            case 4:
                return getFourPartIdentifier(names, resultType);
            default:
                return new DBIdentifier[] { new DBIdentifier(name, resultType) };
        }
    }

    /*
     * Returns a two-part identifier based upon the base identifier type.
     */
    private static DBIdentifier[] getTwoPartIdentifier(String[] names, DBIdentifierType baseId) {
        DBIdentifier[] sNames = new DBIdentifier[2];
        DBIdentifierType id0 = DBIdentifierType.DEFAULT;
        DBIdentifierType id1 = baseId;
        if (baseId != DBIdentifierType.COLUMN &&
                baseId != DBIdentifierType.SCHEMA) {
            id0 = DBIdentifierType.SCHEMA;
        }
        else if (baseId == DBIdentifierType.COLUMN) {
            // Length 2, base name column
            id0 = DBIdentifierType.TABLE;
        }
        else if (baseId == DBIdentifierType.SCHEMA) {
            id0 = DBIdentifierType.DATABASE;
        }
        sNames[0] = new DBIdentifier(names[0], id0);
        sNames[1] = new DBIdentifier(names[1], id1);
        return sNames;
    }

    /*
     * Returns a three-part identifier based upon the base identifier type.
     */
    private static DBIdentifier[] getThreePartIdentifier(String[] names, DBIdentifierType baseId) {
        DBIdentifier[] sNames = new DBIdentifier[3];
        DBIdentifierType id0 = DBIdentifierType.DEFAULT;
        DBIdentifierType id1 = DBIdentifierType.DEFAULT;
        DBIdentifierType id2 = baseId;
        if (baseId != DBIdentifierType.SCHEMA &&
            baseId != DBIdentifierType.COLUMN) {
            id0 = DBIdentifierType.DATABASE;
            id1 = DBIdentifierType.SCHEMA;
        }
        else if (baseId == DBIdentifierType.COLUMN) {
            // Length 2, base name column
            id0 = DBIdentifierType.SCHEMA;
            id1 = DBIdentifierType.TABLE;
        }
        sNames[0] = new DBIdentifier(names[0], id0);
        sNames[1] = new DBIdentifier(names[1], id1);
        sNames[2] = new DBIdentifier(names[2], id2);
        return sNames;
    }

    /*
     * Returns a four-part identifier based upon the base identifier type.
     */
    private static DBIdentifier[] getFourPartIdentifier(String[] names, DBIdentifierType baseId) {
        DBIdentifier[] sNames = new DBIdentifier[4];
        DBIdentifierType id0 = DBIdentifierType.DEFAULT;
        DBIdentifierType id1 = DBIdentifierType.DEFAULT;
        DBIdentifierType id2 = DBIdentifierType.DEFAULT;
        DBIdentifierType id3 = baseId;
        if (baseId == DBIdentifierType.COLUMN) {
            id0 = DBIdentifierType.DATABASE;
            id1 = DBIdentifierType.SCHEMA;
            id2 = DBIdentifierType.TABLE;
        }
        sNames[0] = new DBIdentifier(names[0], id0);
        sNames[1] = new DBIdentifier(names[1], id1);
        sNames[2] = new DBIdentifier(names[2], id2);
        sNames[3] = new DBIdentifier(names[3], id3);
        return sNames;
    }

    /**
     * Returns true if the identifier is null or the name is null or empty.
     * @param name
     * @return
     */
    public static boolean isEmpty(DBIdentifier name) {
        if (isNull(name)) {
            return true;
        }
        return StringUtils.isEmpty(name.getName());
    }

    
    /**
     * Returns true if the identifier is null.
     * @param name
     * @return
     */
    public static boolean isNull(DBIdentifier name) {
        return (name == null || name.getType() == DBIdentifierType.NULL);
    }

    /**
     * Clones an identifier using deep copy.
     */
    public DBIdentifier clone() {
        DBIdentifier sName = new DBIdentifier();
        sName.setNameInternal(getNameInternal());
        sName.setType(getType());
        sName.setIgnoreCase(getIgnoreCase());
        return sName;
    }
    
    /*
     * Internal method to set the base name and avoid normalizing an already 
     * normalized name.
     * @param name
     */
    private void setNameInternal(String name) {
        super.setName(name);
    }

    /*
     * Internal method to get the base name. 
     * normalized name.
     * @param name
     */
    private String getNameInternal() {
        return super.getName();
    }

    /**
     * Returns a copy of an identifier with name trimmed to null.
     * @param name
     * @return
     */
    public static DBIdentifier trimToNull(DBIdentifier name) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        sName.setNameInternal(StringUtils.trimToNull(sName.getNameInternal()));
        return sName;
    }

    /**
     * Equality operation for identifiers.  Supports comparison with strings
     * and objects of this type.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof DBIdentifier) {
            DBIdentifier sName = (DBIdentifier)obj;
            return this.equals(sName, getIgnoreCase() || sName.getIgnoreCase());
        } else if (obj instanceof String) {
            return obj.equals(this.getNameInternal());
        }
        throw new IllegalArgumentException("Unsupported type comparison: " + obj.getClass().getName());
    }

    /**
     * Returns true if this object is NULL or has a null name component.
     * @return
     */
    public boolean isNull() {
        return (_type == DBIdentifierType.NULL || getName() == null);
    }

    /**
     * Comparison operator for identifiers.
     */
    public int compareTo(Identifier o) {
        if (o instanceof DBIdentifier) {
            if (this == DBIdentifier.NULL && (o == null || o == DBIdentifier.NULL)) {
                return 0;
            }
            if (this == DBIdentifier.NULL)
                return 1;
            if (o == null || o == DBIdentifier.NULL)
                return -1;
        }
        return super.compareTo(o);
    }
    

    /**
     * Converts the provided set of names to an array of identifiers of the 
     * provided type
     * @param columnNames
     * @param id
     * @return
     */
    public static DBIdentifier[] toArray(String[] columnNames, DBIdentifierType id) {
        return toArray(columnNames, id, false);
    }

    /**
     * Converts the provided set of names to an array of identifiers of the 
     * provided type, optionally delimiting the names.
     * @param columnNames
     * @param id
     * @return
     */
    public static DBIdentifier[] toArray(String[] names, DBIdentifierType id, boolean delimit) {
        if (names == null) {
            return null;
        }
        DBIdentifier[] sNames = new DBIdentifier[names.length];
        for (int i = 0; i < names.length; i++) {
            sNames[i] = new DBIdentifier(names[i], id, delimit);
        }
        return sNames;
    }
    
    /**
     * Returns a string array of names based upon the provided set of identifiers.
     * @param sNames
     * @return
     */
    public static String[] toStringArray(DBIdentifier[] sNames) {
        if (sNames == null) {
            return null;
        }
        String[] names = new String[sNames.length];
        for (int i = 0; i < sNames.length; i++) {
            names[i] = sNames[i].getName();
        }
        return names;
    }

    /**
     * Constructs a new identifier of type Catalog.
     */
    public static DBIdentifier newCatalog(String name) {
        return newIdentifier(name, DBIdentifierType.CATALOG);
    }

    /**
     * Constructs a new identifier of type Catalog ,optionally delimiting
     * the name.
     */
    public static DBIdentifier newCatalog(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.CATALOG, false, delimit);
    }
    
    /**
     * Constructs a new identifier of type Table.
     */
    public static DBIdentifier newTable(String name) {
        return newIdentifier(name, DBIdentifierType.TABLE);
    }

    /**
     * Constructs a new identifier of type Table, optionally delimiting
     * the name.
     */
    public static DBIdentifier newTable(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.TABLE, false, delimit);
    }
    
    /**
     * Constructs a new identifier of type Column.
     */
    public static DBIdentifier newColumn(String name) {
        return newIdentifier(name, DBIdentifierType.COLUMN);
    }

    /**
     * Constructs a new identifier of type Column,optionally delimiting
     * the name.
     */
    public static DBIdentifier newColumn(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.COLUMN, false, delimit);
    }

    /**
     * Constructs a new identifier of type Schema.
     */
    public static DBIdentifier newSchema(String name) {
        return newIdentifier(name, DBIdentifierType.SCHEMA);
    }

    /**
     * Constructs a new identifier of type Schema, optionally delimiting
     * the name.
     */
    public static DBIdentifier newSchema(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.SCHEMA, false, delimit);
    }

    /**
     * Constructs a new identifier of type Index.
     */
    public static DBIdentifier newIndex(String name) {
        return newIdentifier(name, DBIdentifierType.INDEX);
    }

    /**
     * Constructs a new identifier of type Index, optionally delimiting
     * the name.
     */
    public static DBIdentifier newIndex(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.INDEX, false, delimit);
    }

    /**
     * Constructs a new identifier of type Sequence.
     */
    public static DBIdentifier newSequence(String name) {
        return newIdentifier(name, DBIdentifierType.SEQUENCE);
    }

    /**
     * Constructs a new identifier of type Sequence, optionally delimiting
     * the name.
     */
    public static DBIdentifier newSequence(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.SEQUENCE, false, delimit);
    }

    /**
     * Constructs a new identifier of type ForeignKey.
     */
    public static DBIdentifier newForeignKey(String name) {
        return newIdentifier(name, DBIdentifierType.FOREIGN_KEY);
    }

    /**
     * Constructs a new identifier of type ForeignKey, optionally delimiting
     * the name.
     */
    public static DBIdentifier newForeignKey(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.FOREIGN_KEY, false, delimit);
    }

    /**
     * Constructs a new identifier of type Constraint.
     */
    public static DBIdentifier newConstraint(String name) {
        return newIdentifier(name, DBIdentifierType.CONSTRAINT);
    }

    /**
     * Constructs a new identifier of type Constraint, optionally delimiting
     * the name.
     */
    public static DBIdentifier newConstraint(String name, boolean delimit) {
        return newIdentifier(name, DBIdentifierType.CONSTRAINT, false, delimit);
    }

    /**
     * Constructs a new identifier of type Constant.
     */
    public static DBIdentifier newConstant(String name) {
        return newIdentifier(name, DBIdentifierType.CONSTANT);
    }

    /**
     * Constructs a new identifier of type Column Definition.
     */
    public static DBIdentifier newColumnDefinition(String name) {
        return newIdentifier(name, DBIdentifierType.COLUMN_DEFINITION);
    }

    /**
     * Constructs a new identifier of type Default.
     */
    public static DBIdentifier newDefault(String name) {
        return newIdentifier(name, DBIdentifierType.DEFAULT);
    }

    /**
     * Constructs a new identifier with the provided name and type
     */
    protected static DBIdentifier newIdentifier(String name, DBIdentifierType id) {
        return newIdentifier(name, id, false, false);
    }

    /**
     * Constructs a new identifier with the provided name an type. Optionally,
     * converting the name to upper case.
     */
    public static DBIdentifier newIdentifier(String name, DBIdentifierType id, boolean toUpper) {
        return newIdentifier(name, id, toUpper, false );
    }

    /**
     * Constructs a new identifier (potentially a compound QualifiedDBIdentifier) with the provided 
     * name an type. Optionally, converting the name to upper case and delimiting it.
     */
    protected static DBIdentifier newIdentifier(String name, DBIdentifierType id, boolean toUpper, boolean delimit) {
        return newIdentifier(name,id, toUpper, delimit, false);
    }

    /**
     * Constructs a new identifier (potentially a compound QualifiedDBIdentifier) with the provided 
     * name an type. Optionally, converting the name to upper case and delimiting it.
     */
    protected static DBIdentifier newIdentifier(String name, DBIdentifierType id, boolean toUpper, boolean delimit,
        boolean ignoreCase) {
        if (name == null) {
            return DBIdentifier.NULL;
        }
        
        DBIdentifier dbId = DBIdentifier.NULL;
        // Create a DBIDentifier for single component names.  Otherwise, create a QualifiedDBIdentifier.
        if (!_compoundIdentifier[id.ordinal()] || delimit) {
            dbId = new DBIdentifier(name, id, delimit);
            dbId.setIgnoreCase(ignoreCase);
            if (toUpper) {
                return toUpper(dbId);
            }
        } else {
            // Name can be split. Break it up into components and return a path
            DBIdentifier[] sNames = DBIdentifier.split(id, name);
            dbId = new QualifiedDBIdentifier(sNames);
            dbId.setIgnoreCase(ignoreCase);
        }
        return dbId;
    }

    /**
     * Static equality method for comparing two identifiers.
     * @param name1
     * @param name2
     * @return
     */
    public static boolean equal(DBIdentifier name1, DBIdentifier name2) {
        boolean name1Null = DBIdentifier.isNull(name1);
        if (name1Null && DBIdentifier.isNull(name2)) {
            return true;
        }
        if (name1Null) {
            return false;
        }
        return ((DBIdentifier)name1).equals(name2, false);
    }

    
    private void assertNotNull() {
        if (this == DBIdentifier.NULL || getType() == DBIdentifierType.NULL) {
            throw new IllegalStateException("Cannot modify NULL instance");
        }
    }
    
    /**
     * Returns a new DBIdentifier truncated to length
     * @param name
     * @param length
     * @return
     */
    public static DBIdentifier truncate(DBIdentifier name, int length) {
        DBIdentifier sName = name.clone();
        String strName = sName.getNameInternal();
        if (StringUtils.isEmpty(strName)) {
            return sName;
        }
        strName = Normalizer.truncate(strName, length);
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * Returns a new DBIdentifier with the given string appended.
     * @param name
     * @param length
     * @return
     */
    public static DBIdentifier append(DBIdentifier name, String str) {
        DBIdentifier sName = name.clone();
        String strName = sName.getNameInternal();
        strName = Normalizer.append(strName, str);
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * Returns a new DBIdentifier with the given string combined using 
     * delimiting rules and appropriate separators.
     * @param name
     * @param length
     * @return
     */
    public static DBIdentifier combine(DBIdentifier name, String str) {
        DBIdentifier sName = name.clone();
        String strName = sName.getNameInternal();
        strName = Normalizer.combine(strName, str);
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * Returns a new DBIdentifier converted to lower case - if not delimited.
     * @param name
     * @return
     */
    public static DBIdentifier toLower(DBIdentifier name) {
        return toLower(name, false);
    }
    
    /**
     * Returns a new DBIdentifier converted to lower case.  If delimited,
     * force to lower case using force option.
     * @param name
     * @return
     */
    public static DBIdentifier toLower(DBIdentifier name, boolean force) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        if (sName.getNameInternal() == null) {
            return sName;
        }
        // Do not convert delimited names to lower case.  They may have
        // been delimited to preserve case.
        if (force || !Normalizer.isDelimited(sName.getNameInternal())) {
            sName.setNameInternal(sName.getNameInternal().toLowerCase());
        }
        return sName;
    }

    /**
     * Returns a new DBIdentifier converted to upper case - if not delimited.
     * @param name
     * @return
     */
    public static DBIdentifier toUpper(DBIdentifier name) {
        return toUpper(name, false);
    }

    /**
     * Returns a new DBIdentifier converted to upper case.  If delimited,
     * force to upper case using force option.
     * @param name
     * @return
     */
    public static DBIdentifier toUpper(DBIdentifier name, boolean force) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        if (sName.getNameInternal() == null) {
            return sName;
        }
        // Do not convert delimited names to upper case.  They may have
        // been delimited to preserve case.
        if (force || !Normalizer.isDelimited(sName.getNameInternal())) {
            sName.setNameInternal(sName.getNameInternal().toUpperCase());
        }
        return sName;
    }

    /**
     * Returns a new DBIdentifier with the specified leading string removed.
     * @param name
     * @return
     */
    public static DBIdentifier removeLeading(DBIdentifier name, String leadingStr) {
        DBIdentifier sName = name.clone();
        if (isEmpty(sName)) {
            return sName;
        }
        String strName = sName.getNameInternal();
        int leadingLen = leadingStr.length();
        while (strName.startsWith(leadingStr)) {
            strName = strName.substring(leadingLen);
        }
        sName.setNameInternal(strName);
        return sName;
    }
    
    /**
     * Returns a new DBIdentifier with Hungarian notation removed.
     * @param name
     * @return
     */
    public static DBIdentifier removeHungarianNotation(DBIdentifier name) {
        DBIdentifier hName = name.clone();
        if (isEmpty(hName)) {
            return hName;
        }
        String strName = hName.getNameInternal();
        strName = Normalizer.removeHungarianNotation(strName);
        hName.setNameInternal(strName);
        return hName;
    }

    /**
     * Equality operator which ignores case.
     * @param name
     * @return
     */
   public boolean equalsIgnoreCase(DBIdentifier name) {
       return equals(name, true);
   }

   /**
    * Static equality operator which ignores case.
    * @param name
    * @return
    */
    public static boolean equalsIgnoreCase(DBIdentifier name1, DBIdentifier name2) {
        boolean name1Null = DBIdentifier.isNull(name1);
        if (name1Null && DBIdentifier.isNull(name2)) {
            return true;
        }
        if (name1Null) {
            return false;
        }
        return name1.equals(name2, true);
    }

    private boolean equals(DBIdentifier sName, boolean ignoreCase) {
        if (sName.getNameInternal() == null && getNameInternal() == null) {
            return true;
        }
        if (getNameInternal() == null) {
            return false;
        }
        if (getIgnoreCase() || sName.getIgnoreCase() ||
            ignoreCase || !Normalizer.isDelimited(getNameInternal())) {
            return getNameInternal().equalsIgnoreCase(sName.getNameInternal());
        }
        return getNameInternal().equals(sName.getNameInternal());
    }

    /**
     * Returns a new identifier with a combined prefix and name using the standard name
     * concatenation character ('_').
     * @param prefix
     */
    public static DBIdentifier preCombine(DBIdentifier name, String prefix) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        String strName = sName.getNameInternal();
        strName = Normalizer.combine(prefix, strName);
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * Returns a new identifier with delimiters removed.
     * @param name
     * @return
     */
    public static DBIdentifier removeDelimiters(DBIdentifier name) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        if (isEmpty(sName)) {
            return sName;
        }
        String strName = sName.getNameInternal();
        strName = Normalizer.removeDelimiters(strName);
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * Returns a new delimiter with leading and trailing spaces removed.
     * @param name
     * @return
     */
    public static DBIdentifier trim(DBIdentifier name) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        DBIdentifier sName = name.clone();
        if (isEmpty(sName)) {
            return sName;
        }
        String strName = sName.getNameInternal();
        strName = strName.trim();
        sName.setNameInternal(strName);
        return sName;
    }

    /**
     * The length of the name, including delimiting characters.
     */
    public int length() {
        if (DBIdentifier.isNull(this)) {
            return 0;
        }
        return super.length();
    }

    /**
     * Returns true if the identifier is delimited.
     */
    public boolean isDelimited() {
        if (DBIdentifier.isEmpty(this)) {
            return false;
        }
        return Normalizer.isDelimited(getNameInternal());
    }

    /**
     * Combines an array of names names using standard combining rules and
     * returns an identifier of the specified type.
     */
    public static DBIdentifier combine(DBIdentifierType id, String...names) {
        return newIdentifier(Normalizer.combine(names), id);
    }

    /**
     * Returns the unqualified name of this identifier.
     */
    public DBIdentifier getUnqualifiedName() {
        return this;
    }
}
