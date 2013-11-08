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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;

/**
 * Represents a database schema.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class Schema
    implements Comparable<Schema>, Serializable {

    private DBIdentifier _name = DBIdentifier.NULL;
    private SchemaGroup _group = null;
    private Map<DBIdentifier, Table> _tableMap = null;
    private Map<DBIdentifier, Sequence> _seqMap = null;

    // cache
    private Table[] _tables = null;
    private Sequence[] _seqs = null;

    /**
     * Default constructor.
     */
    public Schema() {
    }

    /**
     * Constructor.
     *
     * @param name the schema name, if any
     * @param group the schema's owning group
     * @deprecated
     */
    public Schema(String name, SchemaGroup group) {
        this(DBIdentifier.newSchema(name), group);
    }

    public Schema(DBIdentifier name, SchemaGroup group) {
        setIdentifier(name);
        _group = group;
    }

    /**
     * Called when the schema is removed from its group. Invalidates the
     * schema and removes all its member tables.
     */
    void remove() {
        Table[] tabs = getTables();
        for (int i = 0; i < tabs.length; i++)
            removeTable(tabs[i]);
        Sequence[] seqs = getSequences();
        for (int i = 0; i < seqs.length; i++)
            removeSequence(seqs[i]);
        _group = null;
    }

    /**
     * Return the schema's group.
     */
    public SchemaGroup getSchemaGroup() {
        return _group;
    }

    /**
     * Return the name of the schema, or null if none.
     * @deprecated
     */
    public String getName() {
        return getIdentifier().getName();
    }

    public DBIdentifier getIdentifier() {
        return _name;
    }

    /**
     * Set the name of the schema. This method can only be used for schemas
     * not attached to a group.
     * @deprecated
     */
    public void setName(String name) {
        setIdentifier(DBIdentifier.trimToNull(DBIdentifier.newSchema(name)));
    }

    public void setIdentifier(DBIdentifier name) {
        if (getSchemaGroup() != null)
            throw new IllegalStateException();
        _name = DBIdentifier.trimToNull(name);
    }
    
    /**
     * Return the schema's tables.
     */
    public Table[] getTables() {
        if (_tables == null)
            _tables = (_tableMap == null) ? new Table[0] : (Table[])
                _tableMap.values().toArray(new Table[_tableMap.size()]);
        return _tables;
    }

    /**
     * Return the table with the given name, or null if none.
     * @deprecated
     */
    public Table getTable(String name) {
        if (name == null || _tableMap == null)
            return null;
        return getTable(DBIdentifier.newIdentifier(name, DBIdentifierType.TABLE, true));
    }

    public Table getTable(DBIdentifier name) {
        if (DBIdentifier.isNull(name) || _tableMap == null)
            return null;
        DBIdentifier sName = DBIdentifier.toUpper(name);
        return (Table) _tableMap.get(sName);
    }

    /**
     * Add a table to the schema.
     * @deprecated
     */
    public Table addTable(String name) {
        return addTable(DBIdentifier.newTable(name));
    }
    
    public Table addTable(DBIdentifier name) {
        SchemaGroup group = getSchemaGroup();
        Table tab;
        name = name.getUnqualifiedName();
        if (group != null) {
            group.addName(name, true);
            tab = group.newTable(name, this);
        } else
            tab = new Table(name, this);
        if (_tableMap == null)
            _tableMap = new TreeMap<DBIdentifier, Table>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _tableMap.put(sName, tab);
        _tables = null;
        return tab;
    }
    

    /**
     * Add a table with a shortened (i.e., validated) name to the schema
     * @deprecated
     */
    public Table addTable(String name, String validName) {
        return addTable(DBIdentifier.newTable(name), DBIdentifier.newTable(validName));
    }

    public Table addTable(DBIdentifier name, DBIdentifier validName) {
        SchemaGroup group = getSchemaGroup();
        Table tab;
        if (group != null) {
            group.addName(validName, true);
            tab = group.newTable(validName, this);
        } else
            tab = new Table(validName, this);
        if (_tableMap == null)
            _tableMap = new TreeMap<DBIdentifier, Table>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _tableMap.put(sName, tab);
        _tables = null;
        return tab;
    }

    /**
     * Remove the given table from the schema.
     *
     * @return true if the table was removed, false if not in the schema
     */
    public boolean removeTable(Table tab) {
        if (tab == null || _tableMap == null)
            return false;

        DBIdentifier sName = DBIdentifier.toUpper(tab.getIdentifier());
        Table cur = (Table) _tableMap.get(sName);
        if (!cur.equals(tab))
            return false;

        _tableMap.remove(sName);
        _tables = null;
        SchemaGroup group = getSchemaGroup();
        if (group != null)
            group.removeName(tab.getIdentifier());
        tab.remove();
        return true;
    }

    /**
     * Import a table from another schema.	 Note that this method does
     * <strong>not</strong> import foreign keys, indexes, or unique constraints.
     */
    public Table importTable(Table table) {
        if (table == null)
            return null;

        Table copy = addTable(table.getIdentifier());
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++)
            copy.importColumn(cols[i]);

        copy.importPrimaryKey(table.getPrimaryKey());
        return copy;
    }

    /**
     * Return the schema's sequences.
     */
    public Sequence[] getSequences() {
        if (_seqs == null)
            _seqs = (_seqMap == null) ? new Sequence[0] : (Sequence[])
                _seqMap.values().toArray(new Sequence[_seqMap.size()]);
        return _seqs;
    }

    /**
     * Return the sequence with the given name, or null if none.
     * @deprecated
     */
    public Sequence getSequence(String name) {
        if (name == null || _seqMap == null)
            return null;
        return getSequence(DBIdentifier.newIdentifier(name, DBIdentifierType.SEQUENCE, true));
    }

    public Sequence getSequence(DBIdentifier name) {
        if (DBIdentifier.isNull(name) || _seqMap == null)
            return null;
        
        DBIdentifier sName = DBIdentifier.toUpper(name);
        Sequence seq = (Sequence) _seqMap.get(sName);
        return seq;
    }

    /**
     * Add a sequence to the schema.
     * @deprecated
     */
    public Sequence addSequence(String name) {
        return addSequence(DBIdentifier.newIdentifier(name, DBIdentifierType.SEQUENCE, true));
    }

    public Sequence addSequence(DBIdentifier name) {
        SchemaGroup group = getSchemaGroup();
        Sequence seq;
        if (group != null) {
            group.addName(name, true);
            seq = group.newSequence(name, this);
        } else
            seq = new Sequence(name, this);
        if (_seqMap == null)
            _seqMap = new TreeMap<DBIdentifier, Sequence>();
        
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _seqMap.put(sName, seq);
        _seqs = null;
        return seq;
    }

    /**
     * Remove the given sequence from the schema.
     *
     * @return true if the sequence was removed, false if not in the schema
     */
    public boolean removeSequence(Sequence seq) {
        if (seq == null || _seqMap == null)
            return false;

        DBIdentifier sName = DBIdentifier.toUpper(seq.getIdentifier());
        Sequence cur = (Sequence) _seqMap.get(sName);
        if (!cur.equals(seq))
            return false;

        _seqMap.remove(sName);
        _seqs = null;
        SchemaGroup group = getSchemaGroup();
        if (group != null)
            group.removeName(seq.getIdentifier());
        seq.remove();
        return true;
    }

    /**
     * Import a sequence from another schema.
     */
    public Sequence importSequence(Sequence seq) {
        if (seq == null)
            return null;

        Sequence copy = addSequence(seq.getIdentifier());
        copy.setInitialValue(seq.getInitialValue());
        copy.setIncrement(seq.getIncrement());
        copy.setAllocate(seq.getAllocate());
        return copy;
    }

    public int compareTo(Schema other) {
        DBIdentifier name = getIdentifier();
        DBIdentifier otherName = ((Schema) other).getIdentifier();
        if (DBIdentifier.isNull(name) && DBIdentifier.isNull(otherName)) {
            return 0;
        }
        if (DBIdentifier.isNull(name))
            return 1;
        if (DBIdentifier.isNull(otherName))
            return -1;
        return name.compareTo(otherName);
    }

    public String toString() {
        return getIdentifier().getName();
    }
}
