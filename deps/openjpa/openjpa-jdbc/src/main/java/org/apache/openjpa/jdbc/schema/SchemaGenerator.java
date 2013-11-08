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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.Normalizer;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;

/**
 * The SchemaGenerator creates {@link Schema}s matching the current
 * database. All schemas are added to the current {@link SchemaGroup}.
 *  Note that tables whose name starts with "OPENJPA_" will be not be added
 * to the database schema. This enables the creation of special tables
 * that will never be dropped by the {@link SchemaTool}.
 *
 * @author Abe White
 */
public class SchemaGenerator {

    private static final Localizer _loc = Localizer.forPackage
        (SchemaGenerator.class);

    private final DataSource _ds;
    private final DBDictionary _dict;
    private final Log _log;
    private final Object[][] _allowed;
    private boolean _indexes = true;
    private boolean _fks = true;
    private boolean _pks = true;
    private boolean _seqs = true;
    private boolean _openjpaTables = true;
    private SchemaGroup _group = null;

    private List<Listener> _listeners = null;
    private int _schemaObjects = 0;
    
    private Connection _conn = null;

    /**
     * Constructor.
     *
     * @param conf configuration for connecting to the data store
     */
    public SchemaGenerator(JDBCConfiguration conf) {
        // note: we cannot assert developer capabilities in this tool like
        // we normally do because this class is also used at runtime

        _ds = conf.getDataSource2(null);
        _log = conf.getLog(JDBCConfiguration.LOG_SCHEMA);

        // cache this now so that if the conn pool only has 1 connection we
        // don't conflict later with the open databasemetadata connection
        _dict = conf.getDBDictionaryInstance();

        // create a table of allowed schema and tables to reflect on
        String[] schemaArray = conf.getSchemasList();
        DBIdentifier[] names = new DBIdentifier[schemaArray == null ? 0 : schemaArray.length];
        for (int i = 0; i < names.length; i++) {
            String[] splitName = Normalizer.splitName(schemaArray[i]);
            if (splitName == null || splitName.length == 0) {
                continue;
            }
            if (splitName.length == 1) {
                names[i] = DBIdentifier.newSchema(schemaArray[i]);
            } else {
                names[i] = QualifiedDBIdentifier.newTable(schemaArray[i]);
            }
        }
        _allowed = parseSchemasList(names);
    }

    /**
     * Given a list of schema names and table names (where table names
     * are always of the form schema.table, or just .table if the schema is
     * unknown), creates a table mapping schema name to table list. Returns
     * null if no args are given. If no tables are given for a particular
     * schema, maps the schema name to null.
     */
    private static Object[][] parseSchemasList(DBIdentifier[] args) {
        if (args == null || args.length == 0)
            return null;

        Map<DBIdentifier, Collection<DBIdentifier>> schemas = new HashMap<DBIdentifier, Collection<DBIdentifier>>();
        DBIdentifier schema = DBIdentifier.NULL, table = DBIdentifier.NULL;
        Collection<DBIdentifier> tables = null;
        for (int i = 0; i < args.length; i++) {
            QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(args[i]);
            schema = path.getSchemaName();
            table = path.getIdentifier();

            // if just a schema name, map schema to null
            if (DBIdentifier.isNull(table) && !schemas.containsKey(schema))
                schemas.put(schema, null);
            else if (!DBIdentifier.isNull(table)) {
                tables = schemas.get(schema);
                if (tables == null) {
                    tables = new LinkedList<DBIdentifier>();
                    schemas.put(schema, tables);
                }
                tables.add(table);
            }
        }

        Object[][] parsed = new Object[schemas.size()][2];
        Map.Entry<DBIdentifier, Collection<DBIdentifier>> entry;
        int idx = 0;
        for (Iterator<Map.Entry<DBIdentifier, Collection<DBIdentifier>>> itr = schemas.entrySet().iterator(); 
            itr.hasNext();) {
            entry = itr.next();
            tables = entry.getValue();

            parsed[idx][0] = entry.getKey();
            if (tables != null)
                parsed[idx][1] = tables.toArray(new DBIdentifier[tables.size()]);
            idx++;
        }
        return parsed;
    }

    /**
     * Return whether indexes should be generated. Defaults to true.
     */
    public boolean getIndexes() {
        return _indexes;
    }

    /**
     * Set whether indexes should be generated. Defaults to true.
     */
    public void setIndexes(boolean indexes) {
        _indexes = indexes;
    }

    /**
     * Return whether foreign keys should be generated. Defaults to true.
     */
    public boolean getForeignKeys() {
        return _fks;
    }

    /**
     * Set whether foreign keys should be generated. Defaults to true.
     */
    public void setForeignKeys(boolean fks) {
        _fks = fks;
    }

    /**
     * Return whether primary keys should be generated. Defaults to true.
     */
    public boolean getPrimaryKeys() {
        return _pks;
    }

    /**
     * Set whether primary keys should be generated. Defaults to true.
     */
    public void setPrimaryKeys(boolean pks) {
        _pks = pks;
    }

    /**
     * Return whether sequences should be generated. Defaults to true.
     */
    public boolean getSequences() {
        return _seqs;
    }

    /**
     * Set whether sequences should be generated. Defaults to true.
     */
    public void setSequences(boolean seqs) {
        _seqs = seqs;
    }

    /**
     * Whether to generate info on special tables used by OpenJPA components
     * for bookkeeping. Defaults to true.
     */
    public boolean getOpenJPATables() {
        return _openjpaTables;
    }

    /**
     * Whether to generate info on special tables used by OpenJPA components
     * for bookkeeping. Defaults to true.
     */
    public void setOpenJPATables(boolean openjpaTables) {
        _openjpaTables = openjpaTables;
    }

    /**
     * Return the current schema group.
     */
    public SchemaGroup getSchemaGroup() {
        if (_group == null)
            _group = new SchemaGroup();
        return _group;
    }

    /**
     * Set the schema group to add generated schemas to.
     */
    public void setSchemaGroup(SchemaGroup group) {
        _group = group;
    }

    /**
     * Generate all schemas set in the configuration. This method also
     * calls {@link #generateIndexes}, {@link #generatePrimaryKeys}, and
     * {@link #generateForeignKeys} automatically.
     */
    public void generateSchemas()
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-schemas"));
        generateSchemas((DBIdentifier[])null);
    }

    /**
     * @deprecated
     */
    public void generateSchemas(String[] schemasAndTables)
        throws SQLException {        
        generateSchemas(DBIdentifier.toArray(schemasAndTables, DBIdentifierType.TABLE));
    }
    
    /**
     * Generate the schemas and/or tables named in the given strings.
     * This method calls {@link #generateIndexes},
     * {@link #generatePrimaryKeys}, and {@link #generateForeignKeys}
     * automatically.
     */
    public void generateSchemas(DBIdentifier[] schemasAndTables) throws SQLException {
        fireGenerationEvent(_loc.get("generating-schemas"));
        // generate all schemas and tables
        try {
            getConn();
            Object[][] schemaMap;
            if (schemasAndTables == null || schemasAndTables.length == 0)
                schemaMap = _allowed;
            else
                schemaMap = parseSchemasList(schemasAndTables);

            if (schemaMap == null) {
                generateSchema(DBIdentifier.NULL, (DBIdentifier[]) null);

                // estimate the number of schema objects we will need to visit
                // in order to estimate progress total for any listeners
                int numTables = getTables(null).size();
                _schemaObjects +=
                    numTables + (_pks ? numTables : 0) + (_indexes ? numTables : 0) + (_fks ? numTables : 0);

                if (_pks)
                    generatePrimaryKeys(DBIdentifier.NULL, null);
                if (_indexes)
                    generateIndexes(DBIdentifier.NULL, null);
                if (_fks)
                    generateForeignKeys(DBIdentifier.NULL, null);
                return;
            }

            for (int i = 0; i < schemaMap.length; i++) {
                generateSchema((DBIdentifier) schemaMap[i][0], (DBIdentifier[]) schemaMap[i][1]);
            }

            // generate pks, indexes, fks
            DBIdentifier schemaName = DBIdentifier.NULL;
            DBIdentifier[] tableNames;
            for (int i = 0; i < schemaMap.length; i++) {
                schemaName = (DBIdentifier) schemaMap[i][0];
                tableNames = (DBIdentifier[]) schemaMap[i][1];

                // estimate the number of schema objects we will need to visit
                // in order to estimate progress total for any listeners
                int numTables = (tableNames != null) ? tableNames.length : getTables(schemaName).size();
                _schemaObjects +=
                    numTables + (_pks ? numTables : 0) + (_indexes ? numTables : 0) + (_fks ? numTables : 0);

                if (_pks) {
                    generatePrimaryKeys(schemaName, tableNames);
                }
                if (_indexes) {
                    generateIndexes(schemaName, tableNames);
                }
                if (_fks) {
                    generateForeignKeys(schemaName, tableNames);
                }
            }
        } finally {
            closeConn();
        }
    }

    /**
     * @param name
     * @param tableNames
     * @deprecated
     */
    public void generateSchema(String name, String[] tableNames)
        throws SQLException {
        generateSchema(DBIdentifier.newSchema(name),
            DBIdentifier.toArray(tableNames, DBIdentifierType.TABLE));
    }

    /**
     * Add a fully-constructed {@link Schema} matching the given database
     * schema to the current group. No foreign keys are generated because
     * some keys might span schemas. You must call
     * {@link #generatePrimaryKeys}, {@link #generateIndexes}, and
     * {@link #generateForeignKeys} separately.
     *
     * @param name the database name of the schema, or null for all schemas
     * @param tableNames a list of tables to generate in the schema, or null
     * to generate all tables
     */
    public void generateSchema(DBIdentifier name, DBIdentifier[] tableNames)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-schema", name));

        // generate tables, including columns and primary keys
        DatabaseMetaData meta = _conn.getMetaData();
        try {
            if (tableNames == null)
                generateTables(name, DBIdentifier.NULL, _conn, meta);
            else
                for (int i = 0; i < tableNames.length; i++)
                    generateTables(name, tableNames[i], _conn, meta);

            if (_seqs) {
                generateSequences(name, DBIdentifier.NULL, _conn, meta);
            }
        } finally {
            // some databases require a commit after metadata to release locks
            try {
                _conn.commit();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Generate primary key information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     * @deprecated
     */
    public void generatePrimaryKeys(String schemaName, String[] tableNames)
        throws SQLException {
        generatePrimaryKeys(DBIdentifier.newSchema(schemaName),
            DBIdentifier.toArray(tableNames, DBIdentifierType.TABLE));
    }

    
    /**
     * Generate primary key information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     */
    public void generatePrimaryKeys(DBIdentifier schemaName, DBIdentifier[] tableNames)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-all-primaries", schemaName));

        DatabaseMetaData meta = _conn.getMetaData();
        try {
            if (tableNames == null)
                generatePrimaryKeys(schemaName, null, _conn, meta);
            else
                for (int i = 0; i < tableNames.length; i++)
                    generatePrimaryKeys(schemaName, tableNames[i], _conn, meta);
        } finally {
            // some databases require a commit after metadata to release locks
            try {
                _conn.commit();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Generate index information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     * @deprecated
     */
    public void generateIndexes(String schemaName, String[] tableNames) 
        throws SQLException {
        generateIndexes(DBIdentifier.newSchema(schemaName),
            DBIdentifier.toArray(tableNames, DBIdentifierType.TABLE));
    }

    /**
     * Generate index information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     */
    public void generateIndexes(DBIdentifier schemaName, DBIdentifier[] tableNames)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-all-indexes", schemaName));

        DatabaseMetaData meta = _conn.getMetaData();
        try {
            if (tableNames == null)
                generateIndexes(schemaName, null, _conn, meta);
            else
                for (int i = 0; i < tableNames.length; i++)
                    generateIndexes(schemaName, tableNames[i], _conn, meta);
        } finally {
            // some databases require a commit after metadata to release locks
            try {
                _conn.commit();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Generate foreign key information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     * @deprecated
     */
    public void generateForeignKeys(String schemaName, String[] tableNames)
        throws SQLException {
        generateForeignKeys(DBIdentifier.newSchema(schemaName),
            DBIdentifier.toArray(tableNames, DBIdentifierType.TABLE));
    }

    
    /**
     * Generate foreign key information for the given schema. This method
     * must be called in addition to {@link #generateSchema}. It should
     * only be called after all schemas are generated. The schema name and
     * tables array can be null to indicate that indexes should be generated
     * for all schemas and/or tables.
     */
    public void generateForeignKeys(DBIdentifier schemaName, DBIdentifier[] tableNames)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-all-foreigns", schemaName));

        DatabaseMetaData meta = _conn.getMetaData();
        try {
            if (tableNames == null)
                generateForeignKeys(schemaName, null, _conn, meta);
            else
                for (int i = 0; i < tableNames.length; i++)
                    generateForeignKeys(schemaName, tableNames[i], _conn, meta);
        } finally {
            // some databases require a commit after metadata to release locks
            try {
                _conn.commit();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * @deprecated
     */
    public void generateTables(String schemaName, String tableName,
        Connection conn, DatabaseMetaData meta) 
        throws SQLException {
        generateTables(DBIdentifier.newSchema(schemaName),
            DBIdentifier.newTable(tableName), conn, meta);
    }

    /**
     * Adds all tables matching the given name pattern to the schema.
     */
    public void generateTables(DBIdentifier schemaName, DBIdentifier tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-columns", schemaName,
            tableName));
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("gen-tables", schemaName, tableName));

        Column[] cols = _dict.getColumns(meta, 
            DBIdentifier.newCatalog(conn.getCatalog()), schemaName,
            tableName, null, conn);

        // when we want to get all the columns for all tables, we need to build
        // a list of tables to verify because some databases (e.g., Postgres)
        // will include indexes in the list of columns, and there is no way to
        // distinguish the indexes from proper columns
        Set<DBIdentifier> tableNames = null;
        if (DBIdentifier.isNull(tableName) || "%".equals(tableName.getName())) {
            Table[] tables = _dict.getTables(meta, DBIdentifier.newCatalog(conn.getCatalog()),
                schemaName, tableName, conn);
            tableNames = new HashSet<DBIdentifier>();
            for (int i = 0; tables != null && i < tables.length; i++) {
                if (cols == null) {
                    tableNames.add(tables[i].getIdentifier());
                }
                else {
                    DBIdentifier sName = DBIdentifier.toUpper(tables[i].getIdentifier());
                    tableNames.add(sName);
                }
            }
        }

        // if database can't handle null table name, recurse on each known name
        if (cols == null && DBIdentifier.isNull(tableName)) {
            for (Iterator<DBIdentifier> itr = tableNames.iterator(); itr.hasNext();)
                generateTables(schemaName, itr.next(), conn, meta);
            return;
        }

        SchemaGroup group = getSchemaGroup();
        Schema schema;
        Table table;
        DBIdentifier tableSchema = DBIdentifier.NULL;
        DBIdentifier baseTableName = (tableName == null) ? DBIdentifier.NULL : tableName.clone();
        for (int i = 0; cols != null && i < cols.length; i++) {
            if (DBIdentifier.isNull(baseTableName) || baseTableName.equals("%")) {
                tableName = cols[i].getTableIdentifier();
            } else {
                tableName = baseTableName;
            }
            if (DBIdentifier.isNull(schemaName)) {
                tableSchema = DBIdentifier.trimToNull(cols[i].getSchemaIdentifier());
            }
            else {
                tableSchema = schemaName;
            }
            
            // ignore special tables
            if (!_openjpaTables &&
                (tableName.getName().toUpperCase().startsWith("OPENJPA_")
                    || tableName.getName().toUpperCase().startsWith("JDO_"))) // legacy
                continue;
            if (_dict.isSystemTable(tableName, tableSchema, !DBIdentifier.isNull(schemaName)))
                continue;

            // ignore tables not in list, or not allowed by schemas property
            
            if (tableNames != null
                && !tableNames.contains(DBIdentifier.toUpper(tableName)))
                continue;
            if (!isAllowedTable(tableSchema, tableName))
                continue;

            schema = group.getSchema(tableSchema);
            if (schema == null)
                schema = group.addSchema(tableSchema);

            table = schema.getTable(tableName);
            if (table == null) {
                table = schema.addTable(tableName);
                if (_log.isTraceEnabled())
                    _log.trace(_loc.get("col-table", table));
            }

            if (_log.isTraceEnabled())
                _log.trace(_loc.get("gen-column", cols[i].getIdentifier(), table));

            if (table.getColumn(cols[i].getIdentifier()) == null) {
                table.importColumn(cols[i]);
            }
        }
    }

    /**
     * Return whether the given table is allowed by the user's schema list.
     */
    private boolean isAllowedTable(DBIdentifier schema, DBIdentifier table) {
        if (_allowed == null)
            return true;

        // do case-insensitive comparison on allowed table and schema names
        DBIdentifier[] tables;
        DBIdentifier[] anySchemaTables = null;
        for (int i = 0; i < _allowed.length; i++) {
            if (_allowed[i][0] == null) {
                anySchemaTables = (DBIdentifier[]) _allowed[i][1];
                if (schema == null)
                    break;
                continue;
            }
            if (!schema.equals((DBIdentifier) _allowed[i][0]))
                continue;

            if (table == null)
                return true;
            tables = (DBIdentifier[]) _allowed[i][1];
            if (tables == null)
                return true;
            for (int j = 0; j < tables.length; j++)
                if (table.equals(tables[j]))
                    return true;
        }

        if (anySchemaTables != null) {
            if (table == null)
                return true;
            for (int i = 0; i < anySchemaTables.length; i++)
                if (table.equals(anySchemaTables[i]))
                    return true;
        }
        return false;
    }

    /**
     * Generates table primary keys.
     * @deprecated
     */
    public void generatePrimaryKeys(String schemaName, String tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        generatePrimaryKeys(DBIdentifier.newSchema(schemaName), DBIdentifier.newTable(tableName),
            conn, meta);
    }

    public void generatePrimaryKeys(DBIdentifier schemaName, DBIdentifier tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-primary",
            schemaName, tableName));
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("gen-pks", schemaName, tableName));

        // if looking for a non-existant table, just return
        SchemaGroup group = getSchemaGroup();
        if (tableName != null && !tableName.isNull() && 
                group.findTable(QualifiedDBIdentifier.getPath(tableName)) == null)
            return;

        // if the database can't use a table name wildcard, recurse on each
        // concrete table in the requested schema(s)
        PrimaryKey[] pks = _dict.getPrimaryKeys(meta, 
            DBIdentifier.newCatalog(conn.getCatalog()),
            schemaName, tableName, conn);
        Table table;
        if (pks == null && tableName == null) {
            Collection<Table> tables = getTables(schemaName);
            for (Iterator<Table> itr = tables.iterator(); itr.hasNext();) {
                table = (Table) itr.next();
                generatePrimaryKeys(table.getSchemaIdentifier(),
                    table.getIdentifier(), conn, meta);
            }
            return;
        }

        Schema schema;
        PrimaryKey pk;
        DBIdentifier name = DBIdentifier.NULL;
        DBIdentifier colName = DBIdentifier.NULL;
        for (int i = 0; pks != null && i < pks.length; i++) {
            schemaName = DBIdentifier.trimToNull(schemaName);
            schema = group.getSchema(schemaName);
            if (schema == null)
                continue;
            table = schema.getTable(pks[i].getTableIdentifier());
            if (table == null)
                continue;

            colName = pks[i].getColumnIdentifier();
            name = pks[i].getIdentifier();
            if (_log.isTraceEnabled())
                _log.trace(_loc.get("gen-pk", name, table, colName));

            pk = table.getPrimaryKey();
            if (pk == null)
                pk = table.addPrimaryKey(name);
            pk.addColumn(table.getColumn(colName));
        }
    }

    /**
     * Generates table indexes.
     * @deprecated
     */
    public void generateIndexes(String schemaName, String tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        generateIndexes(DBIdentifier.newSchema(schemaName), DBIdentifier.newTable(tableName),
            conn, meta);
    }

    public void generateIndexes(DBIdentifier schemaName, DBIdentifier tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-indexes",
            schemaName, tableName));
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("gen-indexes", schemaName, tableName));

        // if looking for a non-existant table, just return
        SchemaGroup group = getSchemaGroup();
        if (tableName != null && group.findTable(QualifiedDBIdentifier.getPath(tableName)) == null)
            return;

        // if the database can't use a table name wildcard, recurse on each
        // concrete table in the requested schema(s)
        Index[] idxs = _dict.getIndexInfo(meta, DBIdentifier.newCatalog(conn.getCatalog()),
            schemaName, tableName, false, true, conn);
        Table table;
        if (idxs == null && tableName == null) {
            Collection<Table> tables = getTables(schemaName);
            for (Iterator<Table> itr = tables.iterator(); itr.hasNext();) {
                table = itr.next();
                generateIndexes(table.getSchemaIdentifier(),
                    table.getIdentifier(), conn, meta);
            }
            return;
        }

        Schema schema;
        Index idx;
        DBIdentifier name = DBIdentifier.NULL;
        DBIdentifier colName = DBIdentifier.NULL;
        DBIdentifier pkName = DBIdentifier.NULL;
        for (int i = 0; idxs != null && i < idxs.length; i++) {
            schemaName = DBIdentifier.trimToNull(idxs[i].getSchemaIdentifier());
            schema = group.getSchema(schemaName);
            if (schema == null)
                continue;
            table = schema.getTable(idxs[i].getTableIdentifier());
            if (table == null)
                continue;

            if (table.getPrimaryKey() != null)
                pkName = table.getPrimaryKey().getIdentifier();
            else
                pkName = null;

            // statistics don't have names; skip them
            name = idxs[i].getIdentifier();
            if (DBIdentifier.isEmpty(name)
                || (pkName != null && name.equals(pkName))
                || _dict.isSystemIndex(name, table))
                continue;

            colName = idxs[i].getColumnIdentifier();
            if (table.getColumn(colName) == null)
                continue;

            if (_log.isTraceEnabled())
                _log.trace(_loc.get("gen-index", name, table, colName));

            // same index may have multiple rows for multiple cols
            idx = table.getIndex(name);
            if (idx == null) {
                idx = table.addIndex(name);
                idx.setUnique(idxs[i].isUnique());
            }
            idx.addColumn(table.getColumn(colName));
        }
    }

    /**
     * Generates table foreign keys.
     */
    public void generateForeignKeys(String schemaName, String tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        generateForeignKeys(DBIdentifier.newSchema(schemaName), DBIdentifier.newTable(tableName),
            conn, meta);
    }

    public void generateForeignKeys(DBIdentifier schemaName, DBIdentifier tableName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-foreign",
            schemaName, tableName));
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("gen-fks", schemaName, tableName));

        // if looking for a non-existant table, just return
        SchemaGroup group = getSchemaGroup();
        if (!DBIdentifier.isNull(tableName) && group.findTable(QualifiedDBIdentifier.getPath(tableName)) == null)
            return;

        // if the database can't use a table name wildcard, recurse on each
        // concrete table in the requested schema(s)
        ForeignKey[] fks = _dict.getImportedKeys(meta, DBIdentifier.newCatalog(conn.getCatalog()),
            schemaName, tableName, conn);
        Table table;
        if (fks == null && DBIdentifier.isNull(tableName)) {
            Collection<Table> tables = getTables(schemaName);
            for (Iterator<Table> itr = tables.iterator(); itr.hasNext();) {
                table = itr.next();
                generateForeignKeys(table.getSchemaIdentifier(),
                    table.getIdentifier(), conn, meta);
            }
            return;
        }

        Schema schema;
        Table pkTable;
        ForeignKey fk;
        DBIdentifier name = DBIdentifier.NULL;
        DBIdentifier pkSchemaName = DBIdentifier.NULL;
        DBIdentifier pkTableName = DBIdentifier.NULL;
        DBIdentifier pkColName = DBIdentifier.NULL;
        DBIdentifier fkColName = DBIdentifier.NULL;
        int seq;
        boolean seqWas0 = false; // some drivers incorrectly start at 0
        Collection<ForeignKey> invalids = null;
        for (int i = 0; fks != null && i < fks.length; i++) {
            schemaName = DBIdentifier.trimToNull(fks[i].getSchemaIdentifier());
            schema = group.getSchema(schemaName);
            if (schema == null)
                continue;
            table = schema.getTable(fks[i].getTableIdentifier());
            if (table == null)
                continue;

            name = fks[i].getIdentifier();
            fkColName = fks[i].getColumnIdentifier();
            pkColName = fks[i].getPrimaryKeyColumnIdentifier();
            seq = fks[i].getKeySequence();
            if (seq == 0)
                seqWas0 = true;
            if (seqWas0)
                seq++;

            // find pk table
            pkSchemaName = fks[i].getPrimaryKeySchemaIdentifier();
            if(_dict.getTrimSchemaName()) {
                pkSchemaName= DBIdentifier.trimToNull(pkSchemaName);
            }
            pkTableName = fks[i].getPrimaryKeyTableIdentifier();
            if (_log.isTraceEnabled())
                _log.trace(_loc.get("gen-fk", new Object[]{ name, table,
                    fkColName, pkTableName, pkColName, seq + "" }));

            pkTable = group.findTable(QualifiedDBIdentifier.newPath(pkSchemaName, pkTableName));
            if (pkTable == null)
                throw new SQLException(_loc.get("gen-nofktable",
                    table, pkTableName).getMessage());

            // this sucks, because it is *not* guaranteed to work;
            // the fk resultset is ordered by primary key table, then
            // sequence number within the foreign key (some drivers don't
            // use sequence numbers correctly either); since fk names
            // are allowed to be null, all this adds up to the fact
            // that there is absolutely no way to distinguish between
            // multiple multi-column fks to the same table; we're going
            // to rely on fk name here and hope it works
            fk = table.getForeignKey(name);

            // start a new fk?
            if (seq == 1 || fk == null) {
                fk = table.addForeignKey(name);
                fk.setDeferred(fks[i].isDeferred());
                fk.setDeleteAction(fks[i].getDeleteAction());
            }

            if (invalids == null || !invalids.contains(fk)) {
                try {
                    Column fkCol = table.getColumn(fkColName);
                    if (fkCol == null) {
                        throw new IllegalArgumentException(_loc.get(
                            "no-column", fkColName, table.getIdentifier())
                            .getMessage());
                    }
                    fk.join(fkCol, pkTable.getColumn(pkColName));
                } catch (IllegalArgumentException iae) {
                    if (_log.isWarnEnabled())
                        _log.warn(_loc.get("bad-join", iae.toString()));
                    if (invalids == null)
                        invalids = new HashSet<ForeignKey>();
                    invalids.add(fk);
                }
            }
        }

        // remove invalid fks
        if (invalids != null) {
            for (Iterator<ForeignKey> itr = invalids.iterator(); itr.hasNext();) {
                fk = itr.next();
                fk.getTable().removeForeignKey(fk);
            }
        }
    }

    /**
     * Adds all sequences matching the given name pattern to the schema.
     * @deprecated
     */
    public void generateSequences(String schemaName, String sequenceName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        generateSequences(DBIdentifier.newSchema(schemaName), 
            DBIdentifier.newSequence(sequenceName), conn, meta);
    }
    
    public void generateSequences(DBIdentifier schemaName, DBIdentifier sequenceName,
        Connection conn, DatabaseMetaData meta)
        throws SQLException {
        fireGenerationEvent(_loc.get("generating-sequences", schemaName));
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("gen-seqs", schemaName, sequenceName));

        // since all the sequences are generated under the default schema
        // therefore, we can use the null schemaname to search
        Sequence[] seqs = _dict.getSequences(meta, DBIdentifier.newCatalog(conn.getCatalog()),
            DBIdentifier.NULL, sequenceName, conn);

        SchemaGroup group = getSchemaGroup();
        Schema schema;
        DBIdentifier sequenceSchema = DBIdentifier.NULL;
        for (int i = 0; seqs != null && i < seqs.length; i++) {
            sequenceName = seqs[i].getIdentifier();
            sequenceSchema = DBIdentifier.trimToNull(seqs[i].getSchemaIdentifier());

            // ignore special tables
            String seqUpper = DBIdentifier.toUpper(sequenceName).getName();
            if (!_openjpaTables &&
                (seqUpper.startsWith("OPENJPA_")
                    || seqUpper.startsWith("JDO_"))) // legacy
                continue;
            if (_dict.isSystemSequence(sequenceName, sequenceSchema,
                schemaName != null, conn))
                continue;
            if (!isAllowedTable(sequenceSchema, null))
                continue;

            schema = group.getSchema(sequenceSchema);
            if (schema == null) {
                schema = group.addSchema(sequenceSchema);
            }
            if (schema.getSequence(sequenceName) == null) {
                schema.addSequence(sequenceName);
            }
        }
    }

    /**
     * Notify any listeners that a schema object was generated. Returns
     * true if generation should continue.
     */
    private void fireGenerationEvent(Object schemaObject)
        throws SQLException {
        if (schemaObject == null)
            return;
        if (_listeners == null || _listeners.size() == 0)
            return;

        Event e = new Event(schemaObject, _schemaObjects);
        for (Iterator<Listener> i = _listeners.iterator(); i.hasNext();) {
            Listener l = i.next();
            if (!l.schemaObjectGenerated(e))
                throw new SQLException(_loc.get("refresh-cancelled")
                    .getMessage());
        }
    }

    /**
     * Adds a listener for schema generation events.
     *
     * @param l the listener to add
     */
    public void addListener(Listener l) {
        if (_listeners == null)
            _listeners = new LinkedList<Listener>();
        _listeners.add(l);
    }

    /**
     * Removes a schema generation listener for schema events.
     *
     * @param l the listener to remove
     * @return true if it was successfully removed
     */
    public boolean removeListener(Listener l) {
        return _listeners != null && _listeners.remove(l);
    }

    /**
     * Return all tables for the given schema name, or all tables in
     * the schema group if null is given.
     */
    private Collection<Table> getTables(DBIdentifier schemaName) {
        SchemaGroup group = getSchemaGroup();
        if (!DBIdentifier.isNull(schemaName)) {
            Schema schema = group.getSchema(schemaName);
            if (schema == null)
                return Collections.emptyList();
            return Arrays.asList(schema.getTables());
        }

        Schema[] schemas = group.getSchemas();
        Collection<Table> tables = new LinkedList<Table>();
        for (int i = 0; i < schemas.length; i++)
            tables.addAll(Arrays.asList(schemas[i].getTables()));
        return tables;
    }

    /**
     * A listener for a potentially lengthy schema generation process.
     */
    public static interface Listener {

        boolean schemaObjectGenerated(Event e);
    }

    /**
     * An event corresponding to the generation of a schema object.
     */
    @SuppressWarnings("serial")
    public static class Event
        extends EventObject {

        private final int _total;

        public Event(Object ob, int total) {
            super(ob);
            _total = total;
        }

        public int getTotal() {
            return _total;
        }
    }
    
    private void getConn() throws SQLException {
        if (_conn == null) {
            _conn = _ds.getConnection();
        }
    }

    private void closeConn() throws SQLException {
        if (_conn != null ) {
            if(! _conn.isClosed()) {
                _conn.close();
            }
        }
    }
}
