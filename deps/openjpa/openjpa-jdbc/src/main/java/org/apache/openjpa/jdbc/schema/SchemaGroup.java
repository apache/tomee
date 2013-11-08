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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;

/**
 * Represents a grouping of schemas used in a database.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class SchemaGroup
    extends NameSet
    implements Cloneable {

    private Map<DBIdentifier, Schema> _schemaMap = null;

    // cache
    private Schema[] _schemas = null;

    /**
     * Return all schemas.
     */
    public Schema[] getSchemas() {
        if (_schemas == null)
            _schemas = (_schemaMap == null) ? new Schema[0] : (Schema[])
                _schemaMap.values().toArray(new Schema[_schemaMap.size()]);
        return _schemas;
    }

    /**
     * Return the schema with the given name, or null if none.
     * @deprecated
     */
    public Schema getSchema(String name) {
        if (_schemaMap == null)
            return null;
        return getSchema(DBIdentifier.toUpper(DBIdentifier.newSchema(name)));
    }

    public Schema getSchema(DBIdentifier name) {
        if (_schemaMap == null)
            return null;
        DBIdentifier sName = DBIdentifier.toUpper(name);
        Schema schema = (Schema) _schemaMap.get(sName);
        return schema;
    }

    /**
     * Add a schema to the group.
     */
    public Schema addSchema() {
        return addSchema(DBIdentifier.NULL);
    }

    /**
     * Add a schema to the group.
     */
    public Schema addSchema(DBIdentifier name) {
        addName(name, false);
        Schema schema = newSchema(name);
        DBIdentifier sName = DBIdentifier.toUpper(name);
        if (_schemaMap == null)
            _schemaMap = new HashMap<DBIdentifier, Schema>();
        _schemaMap.put(sName, schema);
        _schemas = null;
        return schema;
    }

    /**
     * @deprecated
     * @param name
     * @return
     */
    public Schema addSchema(String name) {
        return addSchema(DBIdentifier.newSchema(name));
    }
    
    /**
     * Remove the given schema from the group.
     *
     * @return true if the schema was removed, false if not in the group
     */
    public boolean removeSchema(Schema schema) {
        if (schema == null)
            return false;

        DBIdentifier name = DBIdentifier.toUpper(schema.getIdentifier());
        Schema rem = (Schema) _schemaMap.get(name);
        if (schema.equals(rem)) {
            _schemaMap.remove(name);
            removeName(schema.getIdentifier());
            _schemas = null;
            schema.remove();
            return true;
        }
        return false;
    }

    /**
     * Import a schema from another group. Foreign keys are not imported.
     */
    public Schema importSchema(Schema schema) {
        if (schema == null)
            return null;

        Schema copy = addSchema(schema.getIdentifier());
        Sequence[] seqs = schema.getSequences();
        for (int i = 0; i < seqs.length; i++)
            copy.importSequence(seqs[i]);

        Table[] tables = schema.getTables();
        Index[] idxs;
        Unique[] unqs;
        Table tab;
        for (int i = 0; i < tables.length; i++) {
            tab = copy.importTable(tables[i]);
            idxs = tables[i].getIndexes();
            for (int j = 0; j < idxs.length; j++)
                tab.importIndex(idxs[j]);
            unqs = tables[i].getUniques();
            for (int j = 0; j < unqs.length; j++)
                tab.importUnique(unqs[j]);
        }
        return copy;
    }

    /**
     * Return true if the given table is known to exist. While
     * {@link #findTable} may exhibit dynamic behavior in some schema group
     * implementations, this method only returns true if the table has been
     * added to this group or is known to exist in the database.
     */
    public boolean isKnownTable(Table table) {
        return findTable(table) != null;
    }

    /**
     * Return true if the given table is known to exist. While
     * {@link #findTable} may exhibit dynamic behavior in some schema group
     * implementations, this method only returns true if the table has been
     * added to this group or is known to exist in the database.
     * @deprecated
     */
    public boolean isKnownTable(String name) {
        return findTable(name) != null;
    }

      public boolean isKnownTable(QualifiedDBIdentifier path) {
          return findTable(path) != null;
      }
    
    /**
     * Find the equivalent of the given table in this schema group. The
     * given table that may have come from another schema group.
     */
    public Table findTable(Table table) {
        return findTable(table.getQualifiedPath());
    }

    /**
     * Find the table with the given name in the group, using '.' as the
     * catalog separator. Returns null if no table found.
     * @deprecated
     */
    public Table findTable(String name) {
        if (name == null)
            return null;

        return findTable(QualifiedDBIdentifier.getPath(DBIdentifier.newTable(name)));
    }

    public Table findTable(QualifiedDBIdentifier path) {
        if (DBIdentifier.isNull(path)) {
            return null;
        }
        if (!DBIdentifier.isNull(path.getSchemaName())) {
            Schema schema = getSchema(path.getSchemaName());
            if (schema != null)
                return schema.getTable(path.getUnqualifiedName());
            
        } else {
            Schema[] schemas = getSchemas();
            Table tab;
            for (int i = 0; i < schemas.length; i++) {
                tab = schemas[i].getTable(path.getIdentifier());
                if (tab != null)
                    return tab;
            }
        }
        return null;
    }

    /**
     * Find the table with the given name in the group, using '.' as the catalog
     * separator. Returns null if no table found.
     * @deprecated
     */
    public Table findTable(Schema inSchema, String name) {
        if (name == null)
            return null;
        return findTable(inSchema, DBIdentifier.newTable(name), DBIdentifier.NULL);
    }

    public Table findTable(Schema inSchema, DBIdentifier name) {
        if (DBIdentifier.isNull(name))
            return null;
        return findTable(inSchema, QualifiedDBIdentifier.getPath(name), DBIdentifier.NULL);
    }

    public Table findTable(Schema inSchema, DBIdentifier name, DBIdentifier defaultSchemaName) {
        if (DBIdentifier.isNull(name))
            return null;
        return findTable(inSchema, QualifiedDBIdentifier.getPath(name), defaultSchemaName);
    }
    
    public Table findTable(Schema inSchema, QualifiedDBIdentifier path, DBIdentifier defaultSchemaName) {
        if (path == null)
            return null;

        if (!DBIdentifier.isNull(path.getSchemaName())) {
            Schema schema = getSchema(path.getSchemaName());
            if (schema != null)
                return schema.getTable(path.getIdentifier());
        } else {
            Schema[] schemas = getSchemas();
            for (int i = 0; i < schemas.length; i++) {
                Table tab = schemas[i].getTable(path.getIdentifier());
                // if a table is found and it has the same schema
                // as the input schema , it means that the table
                // exists. However, if the input schema is null,
                // then we assume that there is only one table for the
                // db default schema, in this case, table exists..
                // We can't handle the case that one entity has schema name
                // and other entity does not have schema name but both entities
                // map to the same table.
                boolean isDefaultSchema = DBIdentifier.isNull(inSchema.getIdentifier()) && 
                    !DBIdentifier.isNull(defaultSchemaName) && 
                    DBIdentifier.equalsIgnoreCase(defaultSchemaName, schemas[i].getIdentifier());
                boolean hasNoDefaultSchema = DBIdentifier.isNull(inSchema.getIdentifier()) && 
                    DBIdentifier.isNull(defaultSchemaName); 
                
                if (tab != null &&
                    (schemas[i] == inSchema || isDefaultSchema || hasNoDefaultSchema)) 
                    return tab;

            }
        }
        return null;
    }

    /**
     * Return true if the given sequence is known to exist. While
     * {@link #findSequence} may exhibit dynamic behavior in some schema group
     * implementations, this method only returns true if the sequence has been
     * added to this group or is known to exist in the database.
     */
    public boolean isKnownSequence(Sequence seq) {
        return findSequence(seq) != null;
    }

    /**
     * Return true if the given sequence is known to exist. While
     * {@link #findSequence} may exhibit dynamic behavior in some schema group
     * implementations, this method only returns true if the sequence has been
     * added to this group or is known to exist in the database.
     * @deprecated
     */
    public boolean isKnownSequence(String name) {
        return findSequence(name) != null;
    }

    public boolean isKnownSequence(DBIdentifier name) {
        return findSequence(QualifiedDBIdentifier.getPath(name)) != null;
    }

    public boolean isKnownSequence(QualifiedDBIdentifier path) {
        return findSequence(path) != null;
    }

    /**
     * Find the equivalent of the given sequence in this schema group. The
     * given sequence that may have come from another schema group.
     */
    public Sequence findSequence(Sequence seq) {
        return findSequence(seq.getQualifiedPath());
    }

    /**
     * Find the sequence with the given name in the group, using '.' as the
     * catalog separator. Returns null if no sequence found.
     * @deprecated
     */
    public Sequence findSequence(String name) {
        if (name == null)
            return null;
        return findSequence(DBIdentifier.newSequence(name));
    }

    public Sequence findSequence(DBIdentifier name) {
        if (DBIdentifier.isNull(name))
            return null;
        return findSequence(QualifiedDBIdentifier.getPath(name));
    }

    public Sequence findSequence(QualifiedDBIdentifier path) {
        if (path == null)
            return null;

        if (!DBIdentifier.isNull(path.getSchemaName())) {
            Schema schema = getSchema(path.getSchemaName());
            if (schema != null)
                return schema.getSequence(path.getIdentifier());
        } else {
            Schema[] schemas = getSchemas();
            Sequence seq;
            for (int i = 0; i < schemas.length; i++) {
                seq = schemas[i].getSequence(path.getIdentifier());
                if (seq != null)
                    return seq;
            }
        }
        return null;
    }

    /**
     * Find the sequence with the given name in the group, using '.' as the
     * catalog separator. Returns null if no sequence found.
     * @deprecated
     */
    public Sequence findSequence(Schema inSchema, String name) {
        if (name == null)
            return null;
        return findSequence(inSchema, QualifiedDBIdentifier.getPath(DBIdentifier.newSequence(name)));
    }

    
    public Sequence findSequence(Schema inSchema, QualifiedDBIdentifier path) {
        if (path == null)
            return null;

        if (!DBIdentifier.isNull(path.getSchemaName())) {
            Schema schema = getSchema(path.getSchemaName());
            if (schema != null)
                return schema.getSequence(path.getIdentifier());
        } else {
            Schema[] schemas = getSchemas();
            Sequence seq;
            for (int i = 0; i < schemas.length; i++) {
                seq = schemas[i].getSequence(path.getIdentifier());
                if ((seq != null) &&
                        (schemas[i] == inSchema || DBIdentifier.isNull(inSchema.getIdentifier())))
                    return seq;
            }
        }
        return null;
    }

    /**
     * Find all foreign keys exported by a given primary key (all foreign keys
     * that link to the primary key).
     */
    public ForeignKey[] findExportedForeignKeys(PrimaryKey pk) {
        if (pk == null)
            return new ForeignKey[0];

        Schema[] schemas = getSchemas();
        Table[] tabs;
        ForeignKey[] fks;
        Collection<ForeignKey> exports = new LinkedList<ForeignKey>();
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                fks = tabs[j].getForeignKeys();
                for (int k = 0; k < fks.length; k++) {
                    if (fks[k].getPrimaryKeyTable() != null
                        && pk.equals(fks[k].getPrimaryKeyTable().
                        getPrimaryKey()))
                        exports.add(fks[k]);
                }
            }
        }
        return (ForeignKey[]) exports.toArray(new ForeignKey[exports.size()]);
    }

    /**
     * Remove unreferenced or emtpy components from the schema.
     */
    public void removeUnusedComponents() {
        Schema[] schemas = getSchemas();
        Table[] tabs;
        Column[] cols;
        Sequence[] seqs;
        PrimaryKey pk;
        ForeignKey[] fks;
        for (int i = 0; i < schemas.length; i++) {
            seqs = schemas[i].getSequences();
            for (int j = 0; j < seqs.length; j++)
                if (seqs[j].getRefCount() == 0)
                    schemas[i].removeSequence(seqs[j]);

            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                pk = tabs[j].getPrimaryKey();
                fks = tabs[j].getForeignKeys();
                cols = tabs[j].getColumns();

                if (pk != null && pk.getRefCount() == 0)
                    tabs[j].removePrimaryKey();

                for (int k = 0; k < fks.length; k++)
                    if (fks[k].getRefCount() == 0)
                        tabs[j].removeForeignKey(fks[k]);

                for (int k = 0; k < cols.length; k++)
                    if (cols[k].getRefCount() == 0)
                        tabs[j].removeColumn(cols[k]);

                if (tabs[j].getColumns().length == 0)
                    schemas[i].removeTable(tabs[j]);
            }

            if (schemas[i].getTables().length == 0)
                removeSchema(schemas[i]);
        }
    }

    public Object clone() {
        SchemaGroup clone = newInstance();
        clone.copy(this);
        return clone;
    }

    /**
     * Create a new instance of this class.
     */
    protected SchemaGroup newInstance() {
        return new SchemaGroup();
    }

    /**
     * Copy cloneable state from the given instance.
     */
    protected void copy(SchemaGroup group) {
        Schema[] schemas = group.getSchemas();
        for (int i = 0; i < schemas.length; i++)
            importSchema(schemas[i]);

        // have to do fks after all schemas are imported
        Table[] tabs;
        ForeignKey[] fks;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                fks = tabs[j].getForeignKeys();
                for (int k = 0; k < fks.length; k++)
                    getSchema(schemas[i].getIdentifier()).getTable
                        (tabs[j].getIdentifier()).importForeignKey(fks[k]);
            }
        }
    }

    /**
     * Return a new schema with the given name.
     * @deprecated
     */
    protected Schema newSchema(String name) {
        return new Schema(name, this);
    }

    protected Schema newSchema(DBIdentifier name) {
        return new Schema(name, this);
    }

    /**
     * Return a new sequence with the given name and owner schema.
     * @deprecated
     */
    protected Sequence newSequence(String name, Schema schema) {
        return new Sequence(name, schema);
    }

    protected Sequence newSequence(DBIdentifier name, Schema schema) {
        return new Sequence(name, schema);
    }

    /**
     * Return a new table with the given name and owner schema.
     * @deprecated
     */
    protected Table newTable(String name, Schema schema) {
        return new Table(name, schema);
    }

    protected Table newTable(DBIdentifier name, Schema schema) {
        return new Table(name, schema);
    }

    /**
     * Return a new column with the given name and owner table.
     * @deprecated
     */
    protected Column newColumn(String name, Table table) {
        return new Column(name, table);
    }

    protected Column newColumn(DBIdentifier name, Table table) {
        return new Column(name, table);
    }

    /**
     * Return a new primary key with the given name and owner table.
     * @deprecated
     */
    protected PrimaryKey newPrimaryKey(String name, Table table) {
        return new PrimaryKey(name, table);
    }

    protected PrimaryKey newPrimaryKey(DBIdentifier name, Table table) {
        return new PrimaryKey(name, table);
    }

    /**
     * Return a new index with the given name and owner table.
     * @deprecated
     */
    protected Index newIndex(String name, Table table) {
        return new Index(name, table);
    }

    protected Index newIndex(DBIdentifier name, Table table) {
        return new Index(name, table);
    }

    /**
     * Return a new unique constraint with the given name and owner table.
     * @deprecated
     */
    protected Unique newUnique(String name, Table table) {
        return new Unique(name, table);
    }

    protected Unique newUnique(DBIdentifier name, Table table) {
        return new Unique(name, table);
    }

    /**
     * Return a new foreign key with the given name and owner table.
     * @deprecated
     */
    protected ForeignKey newForeignKey(String name, Table table) {
        return new ForeignKey(name, table);
    }

    protected ForeignKey newForeignKey(DBIdentifier name, Table table) {
        return new ForeignKey(name, table);
    }

}
