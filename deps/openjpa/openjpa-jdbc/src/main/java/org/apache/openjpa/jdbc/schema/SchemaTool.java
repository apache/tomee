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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.jdbc.DelegatingDataSource;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.MetaDataSerializer;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.InvalidStateException;

/**
 * The SchemaTool is used to manage the database schema. Note that the
 * tool never adds or drops unique constraints from existing tables, because
 * JDBC {@link DatabaseMetaData} does not include information on these
 * constraints.
 *
 * @author Abe White
 * @author Patrick Linskey
 */
public class SchemaTool {

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DROP = "drop";
    public static final String ACTION_RETAIN = "retain";
    public static final String ACTION_REFRESH = "refresh";
    public static final String ACTION_BUILD = "build";
    public static final String ACTION_REFLECT = "reflect";
    public static final String ACTION_CREATEDB = "createDB";
    public static final String ACTION_DROPDB = "dropDB";
    public static final String ACTION_IMPORT = "import";
    public static final String ACTION_EXPORT = "export";
    public static final String ACTION_DELETE_TABLE_CONTENTS = "deleteTableContents";

    public static final String[] ACTIONS = new String[]{
        ACTION_ADD,
        ACTION_DROP,
        ACTION_RETAIN,
        ACTION_REFRESH,
        ACTION_BUILD,
        ACTION_REFLECT,
        ACTION_CREATEDB,
        ACTION_DROPDB,
        ACTION_IMPORT,
        ACTION_EXPORT,
        ACTION_DELETE_TABLE_CONTENTS,
    };

    protected static final Localizer _loc = Localizer.forPackage(SchemaTool.class);

    protected final JDBCConfiguration _conf;
    protected final DataSource _ds;
    protected final Log _log;
    protected final DBDictionary _dict;
    private final String _action;
    private boolean _ignoreErrs = false;
    private boolean _openjpaTables = false;
    private boolean _dropTables = true;
    private boolean _dropSeqs = true;
    private boolean _pks = true;
    private boolean _fks = true;
    private boolean _indexes = true;
    private boolean _seqs = true;
    private PrintWriter _writer = null;
    private SchemaGroup _group = null;
    private SchemaGroup _db = null;
    protected boolean _fullDB = false;
    protected String _sqlTerminator = ";";
    
    /**
     * Default constructor. Tools constructed this way will not have an
     * action, so the {@link #run()} method will be a no-op.
     */
    public SchemaTool(JDBCConfiguration conf) {
        this(conf, null);
    }

    /**
     * Construct a tool to perform the given action.
     */
    public SchemaTool(JDBCConfiguration conf, String action) {
        if (action != null && !Arrays.asList(ACTIONS).contains(action)) {
            Configurations.configureInstance(this, conf, action, action);
        }

        _conf = conf;
        _action = action;
        _ds = ACTION_BUILD.equals(action) ? null : conf.getDataSource2(null);
        _log = conf.getLog(JDBCConfiguration.LOG_SCHEMA);

        // initialize this up-front; otherwise the dbdictionaryfactory might
        // try to take a connection to initialize when we've already got one:
        // bad news if the max pool is 1
        _dict = _conf.getDBDictionaryInstance();
    }

    /**
     * Cleanup DataSource after run()/record()
     */
    public void clear() {
        if (_ds != null && _ds instanceof DelegatingDataSource) {
            try {
                ((DelegatingDataSource)_ds).close();
            } catch (Exception e) {
                // no-op
            }
        }
    }
    
    /**
     * The action supplied on construction.
     */
    public String getAction() {
        return _action;
    }

    /**
     * If true, SQLExceptions thrown during schema manipulation will be
     * printed but ignored.
     */
    public boolean getIgnoreErrors() {
        return _ignoreErrs;
    }

    /**
     * If true, SQLExceptions thrown during schema manipulation will be
     * printed but ignored.
     */
    public void setIgnoreErrors(boolean ignoreErrs) {
        _ignoreErrs = ignoreErrs;
    }

    /**
     * Whether to act on special tables used by OpenJPA components
     * for bookkeeping.
     */
    public boolean getOpenJPATables() {
        return _openjpaTables;
    }

    /**
     * Whether to act on special tables used by OpenJPA components
     * for bookkeeping.
     */
    public void setOpenJPATables(boolean openjpaTables) {
        _openjpaTables = openjpaTables;
    }

    /**
     * If true, tables that appear to be unused will be dropped. Defaults to
     * true.
     */
    public boolean getDropTables() {
        return _dropTables;
    }

    /**
     * If true, tables that appear to be unused will be dropped. Defaults to
     * true.
     */
    public void setDropTables(boolean dropTables) {
        _dropTables = dropTables;
    }

    /**
     * If true, sequences that appear to be unused will be dropped. Defaults
     * to true.
     */
    public boolean getDropSequences() {
        return _dropSeqs;
    }

    /**
     * If true, sequences that appear to be unused will be dropped. Defaults
     * to true.
     */
    public void setDropSequences(boolean dropSeqs) {
        _dropSeqs = dropSeqs;
        if (dropSeqs)
            setSequences(true);
    }

    /**
     * Whether sequences should be manipulated. Defaults to true.
     */
    public boolean getSequences() {
        return _seqs;
    }

    /**
     * Whether sequences should be manipulated. Defaults to true.
     */
    public void setSequences(boolean seqs) {
        _seqs = seqs;
    }

    /**
     * Whether indexes on existing tables should be manipulated.
     * Defaults to true.
     */
    public boolean getIndexes() {
        return _indexes;
    }

    /**
     * Whether indexes on existing tables should be manipulated.
     * Defaults to true.
     */
    public void setIndexes(boolean indexes) {
        _indexes = indexes;
    }

    /**
     * Whether foreign keys on existing tables should be manipulated.
     * Defaults to true.
     */
    public boolean getForeignKeys() {
        return _fks;
    }

    /**
     * Whether foreign keys on existing tables should be manipulated.
     * Defaults to true.
     */
    public void setForeignKeys(boolean fks) {
        _fks = fks;
    }

    /**
     * Whether primary keys on existing tables should be manipulated.
     * Defaults to true.
     */
    public boolean getPrimaryKeys() {
        return _pks;
    }

    /**
     * Whether primary keys on existing tables should be manipulated.
     * Defaults to true.
     */
    public void setPrimaryKeys(boolean pks) {
        _pks = pks;
    }

    /**
     * The stream to write to for the creation of SQL scripts. If the
     * stream is non-null, all SQL will be written to this stream rather than
     * executed against the database.
     */
    public Writer getWriter() {
        return _writer;
    }

    /**
     * The stream to write to for the creation of SQL scripts. If the
     * stream is non-null, all SQL will be written to this stream rather than
     * executed against the database.
     */
    public void setWriter(Writer writer) {
        if (writer == null)
            _writer = null;
        else if (writer instanceof PrintWriter)
            _writer = (PrintWriter) writer;
        else
            _writer = new PrintWriter(writer);
    }
    
    public void setSQLTerminator(String t) {
    	_sqlTerminator = t;
    }

    /**
     * Return the schema group the tool will act on.
     */
    public SchemaGroup getSchemaGroup() {
        return _group;
    }

    /**
     * Set the schema group the tool will act on.
     */
    public void setSchemaGroup(SchemaGroup group) {
        _group = group;
    }

    ///////////
    // Actions
    ///////////

    /**
     * Run the tool action.
     */
    public void run()
        throws SQLException {
        if (_action == null)
            return;

        if (ACTION_ADD.equals(_action))
            add();
        else if (ACTION_DROP.equals(_action))
            drop();
        else if (ACTION_RETAIN.equals(_action))
            retain();
        else if (ACTION_REFRESH.equals(_action))
            refresh();
        else if (ACTION_BUILD.equals(_action))
            build();
        else if (ACTION_CREATEDB.equals(_action))
            createDB();
        else if (ACTION_DROPDB.equals(_action))
            dropDB();
        else if (ACTION_DELETE_TABLE_CONTENTS.equals(_action))
            deleteTableContents();
    }

    /**
     * Adds any components present in the schema repository but absent from
     * the database.
     */
    protected void add()
        throws SQLException {
        add(getDBSchemaGroup(false), assertSchemaGroup());
    }

    /**
     * Drops all schema components in the schema repository that also exist
     * in the database.
     */
    protected void drop()
        throws SQLException {
        drop(getDBSchemaGroup(false), assertSchemaGroup());
    }

    /**
     * Drops database components that are not mentioned in the schema
     * repository.
     */
    protected void retain()
        throws SQLException {
        retain(getDBSchemaGroup(true), assertSchemaGroup(),
            getDropTables(), getDropSequences());
    }

    /**
     * Adds any components present in the schema repository but absent from
     * the database, and drops unused database components.
     */
    protected void refresh()
        throws SQLException {
        SchemaGroup local = assertSchemaGroup();
        SchemaGroup db = getDBSchemaGroup(true);
        retain(db, local, getDropTables(), getDropSequences());
        add(db, local);
    }

    /**
     * Re-execute all SQL used for the creation of the current database;
     * this action is usually used when creating SQL scripts.
     */
    protected void createDB()
        throws SQLException {
        SchemaGroup group = new SchemaGroup();
        group.addSchema();
        add(group, getDBSchemaGroup(true));
    }

    /**
     * Re-execute all SQL used for the creation of the current database;
     * this action is usually used when creating SQL scripts.
     */
    protected void build()
        throws SQLException {
        SchemaGroup group = new SchemaGroup();
        group.addSchema();
        add(group, assertSchemaGroup());
    }

    /**
     * Drop the current database.
     */
    protected void dropDB()
        throws SQLException {
        retain(getDBSchemaGroup(true), new SchemaGroup(), true, true);
    }

    /**
     * Issue DELETE statement against all known tables.
     */
    protected void deleteTableContents() 
        throws SQLException {
        SchemaGroup group = getSchemaGroup();
        Schema[] schemas = group.getSchemas();
        Collection<Table> tables = new LinkedHashSet<Table>();
        for (int i = 0; i < schemas.length; i++) {
            Table[] ts = schemas[i].getTables();
            for (int j = 0; j < ts.length; j++)
                tables.add(ts[j]);
        }
        Table[] tableArray = tables.toArray(new Table[tables.size()]);
        Connection conn = _ds.getConnection();
        try {
            String[] sql = _conf.getDBDictionaryInstance()
                .getDeleteTableContentsSQL(tableArray, conn);
            if (!executeSQL(sql)) {
                _log.warn(_loc.get("delete-table-contents"));
            }
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Record the changes made to the DB in the current {@link SchemaFactory}.
     */
    public void record() {
        if (_db != null && _writer == null)
            _conf.getSchemaFactoryInstance().storeSchema(_db);
    }

    /**
     * Adds all database components in the repository schema that are not
     * present in the given database schema to the database.
     */
    protected void add(SchemaGroup db, SchemaGroup repos)
        throws SQLException {
        // add sequences
        Schema[] schemas = repos.getSchemas();
        Schema schema;
        if (_seqs) {
            Sequence[] seqs;
            for (int i = 0; i < schemas.length; i++) {
                seqs = schemas[i].getSequences();
                for (int j = 0; j < seqs.length; j++) {
                    if (db.findSequence(schemas[i], seqs[j].getQualifiedPath()) !=
                            null)
                        continue;

                    if (createSequence(seqs[j])) {
                        schema = db.getSchema(seqs[j].getSchemaIdentifier());
                        if (schema == null)
                            schema = db.addSchema(seqs[j].getSchemaIdentifier());
                        schema.importSequence(seqs[j]);
                    } else
                        _log.warn(_loc.get("add-seq", seqs[j]));
                }
            }
        }

        // order is important in this method; start with columns
        Table[] tabs;
        Table dbTable;
        Column[] cols;
        Column col;
        DBIdentifier defaultSchemaName = DBIdentifier.newSchema(_dict.getDefaultSchemaName());
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                cols = tabs[j].getColumns();
                dbTable = db.findTable(schemas[i], tabs[j].getQualifiedPath(), defaultSchemaName);
                for (int k = 0; k < cols.length; k++) {
                    if (dbTable != null) {
                        DBIdentifier colName = cols[k].getIdentifier();
                        col = dbTable.getColumn(colName);
                        if (col == null) {
                            if (addColumn(cols[k]))
                                dbTable.importColumn(cols[k]);
                            else
                                _log.warn(_loc.get("add-col", cols[k],
                                    tabs[j]));
                        } else if (!cols[k].equalsColumn(col)) {
                            _log.warn(_loc.get("bad-col", new Object[]{
                                col, dbTable, col.getDescription(),
                                cols[k].getDescription() }));
                        }
                    }
                }
            }
        }

        // primary keys
        if (_pks) {
            PrimaryKey pk;
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++) {
                    pk = tabs[j].getPrimaryKey();
                    dbTable = db.findTable(schemas[i], tabs[j].getQualifiedPath());
                    if (pk != null && !pk.isLogical() && dbTable != null) {
                        if (dbTable.getPrimaryKey() == null
                            && addPrimaryKey(pk))
                            dbTable.importPrimaryKey(pk);
                        else if (dbTable.getPrimaryKey() == null)
                            _log.warn(_loc.get("add-pk", pk, tabs[j]));
                        else if (!pk.equalsPrimaryKey(dbTable.getPrimaryKey()))
                            _log.warn(_loc.get("bad-pk",
                                dbTable.getPrimaryKey(), dbTable));
                    }
                }
            }
        }

        // tables
        Set<Table> newTables = new HashSet<Table>();
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                if (db.findTable(schemas[i], tabs[j].getQualifiedPath()) != null)
                    continue;

                if (createTable(tabs[j])) {
                    newTables.add(tabs[j]);
                    schema = db.getSchema(tabs[j].getSchemaIdentifier());
                    if (schema == null)
                        schema = db.addSchema(tabs[j].getSchemaIdentifier());
                    schema.importTable(tabs[j]);
                } else
                    _log.warn(_loc.get("add-table", tabs[j]));
            }
        }

        // indexes
        Index[] idxs;
        Index idx;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                // create indexes on new tables even if indexes
                // have been turned off
                if (!_indexes && !newTables.contains(tabs[j]))
                    continue;

                idxs = tabs[j].getIndexes();
                dbTable = db.findTable(schemas[i], tabs[j].getQualifiedPath());
                for (int k = 0; k < idxs.length; k++) {
                    if (dbTable != null) {
                        idx = findIndex(dbTable, idxs[k]);
                        if (idx == null) {
                            if (createIndex(idxs[k], dbTable, tabs[j].getUniques()))
                                dbTable.importIndex(idxs[k]);
                            else
                                _log.warn(_loc.get("add-index", idxs[k],
                                    tabs[j]));
                        } else if (!idxs[k].equalsIndex(idx))
                            _log.warn(_loc.get("bad-index", idx, dbTable));
                    }
                }
            }
        }

        // Unique Constraints on group of columns
        Unique[] uniques;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                // create unique constraints only on new tables 
                if (!newTables.contains(tabs[j]))
                    continue;

                uniques = tabs[j].getUniques();
                if (uniques == null || uniques.length == 0)
                    continue;
                dbTable = db.findTable(tabs[j]);
                if (dbTable == null)
                    continue;
                for (int k = 0; k < uniques.length; k++) {
                    dbTable.importUnique(uniques[k]);
                }
            }
        }
        
        // foreign keys
        ForeignKey[] fks;
        ForeignKey fk;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                // create foreign keys on new tables even if fks
                // have been turned off
                if (!_fks && !newTables.contains(tabs[j]))
                    continue;

                fks = tabs[j].getForeignKeys();
                dbTable = db.findTable(schemas[i],tabs[j].getQualifiedPath());
                for (int k = 0; k < fks.length; k++) {
                    if (!fks[k].isLogical() && dbTable != null) {
                        fk = findForeignKey(dbTable, fks[k]);
                        if (fk == null) {
                            if (addForeignKey(fks[k]))
                                dbTable.importForeignKey(fks[k]);
                            else
                                _log.warn(_loc.get("add-fk",
                                    fks[k], tabs[j]));
                        } else if (!fks[k].equalsForeignKey(fk))
                            _log.warn(_loc.get("bad-fk", fk, dbTable));
                    }
                }
            }
        }
    }

    /**
     * Drops all database components that are in the given database schema
     * but not in the repository schema.
     */
    protected void retain(SchemaGroup db, SchemaGroup repos, boolean tables,
        boolean sequences)
        throws SQLException {
        Schema[] schemas = db.getSchemas();
        if (_seqs && sequences) {
            Sequence[] seqs;
            for (int i = 0; i < schemas.length; i++) {
                seqs = schemas[i].getSequences();
                for (int j = 0; j < seqs.length; j++) {
                    if (!isDroppable(seqs[j]))
                        continue;
                    if (repos.findSequence(seqs[j]) == null) {
                        if (dropSequence(seqs[j]))
                            schemas[i].removeSequence(seqs[j]);
                        else
                            _log.warn(_loc.get("drop-seq", seqs[j]));
                    }
                }
            }
        }

        // order is important in this method; start with foreign keys
        Table[] tabs;
        Table reposTable;
        if (_fks) {
            ForeignKey[] fks;
            ForeignKey fk;
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++) {
                    if (!isDroppable(tabs[j]))
                        continue;
                    fks = tabs[j].getForeignKeys();
                    reposTable = repos.findTable(tabs[j]);
                    if (!tables && reposTable == null)
                        continue;

                    for (int k = 0; k < fks.length; k++) {
                        if (fks[k].isLogical())
                            continue;

                        fk = null;
                        if (reposTable != null)
                            fk = findForeignKey(reposTable, fks[k]);
                        if (reposTable == null || fk == null
                            || !fks[k].equalsForeignKey(fk)) {
                            if (dropForeignKey(fks[k]))
                                tabs[j].removeForeignKey(fks[k]);
                            else
                                _log.warn(_loc.get("drop-fk", fks[k],
                                    tabs[j]));
                        }
                    }
                }
            }
        }

        // primary keys
        if (_pks) {
            PrimaryKey pk;
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++) {
                    if (!isDroppable(tabs[j]))
                        continue;
                    pk = tabs[j].getPrimaryKey();
                    if (pk != null && pk.isLogical())
                        continue;

                    reposTable = repos.findTable(tabs[j]);
                    if (pk != null && reposTable != null
                        && (reposTable.getPrimaryKey() == null
                        || !pk.equalsPrimaryKey(reposTable.getPrimaryKey()))) {
                        if (dropPrimaryKey(pk))
                            tabs[j].removePrimaryKey();
                        else
                            _log.warn(_loc.get("drop-pk", pk, tabs[j]));
                    }
                }
            }
        }

        // columns
        Column[] cols;
        Column col;
        Collection<Table> drops = new LinkedList<Table>();
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                if (!isDroppable(tabs[j]))
                    continue;
                cols = tabs[j].getColumns();
                reposTable = repos.findTable(tabs[j]);
                if (reposTable != null) {
                    for (int k = 0; k < cols.length; k++) {
                        col = reposTable.getColumn(cols[k].getIdentifier());
                        if (col == null || !cols[k].equalsColumn(col)) {
                            if (tabs[j].getColumns().length == 1)
                                drops.add(tabs[j]);
                            else if (dropColumn(cols[k]))
                                tabs[j].removeColumn(cols[k]);
                            else
                                _log.warn(_loc.get("drop-col", cols[k],
                                    tabs[j]));
                        }
                    }
                }
            }
        }

        // now tables
        if (tables) {
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++)
                    if (!!isDroppable(tabs[j])
                        && repos.findTable(tabs[j]) == null)
                        drops.add(tabs[j]);
            }
        }
        dropTables(drops, db);
    }

    /**
     * Drops all database components in the given repository schema.
     */
    protected void drop(SchemaGroup db, SchemaGroup repos)
        throws SQLException {
        // drop sequences
        Schema[] schemas = repos.getSchemas();
        if (_seqs) {
            Sequence[] seqs;
            Sequence dbSeq;
            for (int i = 0; i < schemas.length; i++) {
                seqs = schemas[i].getSequences();
                for (int j = 0; j < seqs.length; j++) {
                    if (!isDroppable(seqs[j]))
                        continue;
                    dbSeq = db.findSequence(seqs[j]);
                    if (dbSeq != null) {
                        if (dropSequence(seqs[j]))
                            dbSeq.getSchema().removeSequence(dbSeq);
                        else
                            _log.warn(_loc.get("drop-seq", seqs[j]));
                    }
                }
            }
        }

        // calculate tables to drop; we can only drop tables if we're sure
        // the user listed the entire table definition in the stuff they want
        // dropped; else they might just want to drop a few columns
        Collection<Table> drops = new LinkedList<Table>();
        Table[] tabs;
        Table dbTable;
        Column[] dbCols;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            tables:
            for (int j = 0; j < tabs.length; j++) {
                if (!isDroppable(tabs[j]))
                    continue;
                dbTable = db.findTable(tabs[j]);
                if (dbTable == null)
                    continue;

                dbCols = dbTable.getColumns();
                for (int k = 0; k < dbCols.length; k++) {
                    if (!dbCols[k].getIdentifier().getName().equals(_dict.getIdentityColumnName()) &&
                        !tabs[j].containsColumn(dbCols[k]))
                        continue tables;
                }
                drops.add(tabs[j]);
            }
        }

        // order is important in this method; start with foreign keys mentioned
        // in the drop schema
        if (_fks) {
            ForeignKey[] fks;
            ForeignKey fk;
            for (int i = 0; i < schemas.length; i++) {
                tabs = schemas[i].getTables();
                for (int j = 0; j < tabs.length; j++) {
                    if (!isDroppable(tabs[j]))
                        continue;
                    fks = tabs[j].getForeignKeys();
                    dbTable = db.findTable(tabs[j]);
                    for (int k = 0; k < fks.length; k++) {
                        if (fks[k].isLogical())
                            continue;

                        fk = null;
                        if (dbTable != null)
                            fk = findForeignKey(dbTable, fks[k]);
                        if (dbTable == null || fk == null)
                            continue;

                        if (dropForeignKey(fks[k]))
                            if (dbTable != null)
                                dbTable.removeForeignKey(fk);
                            else
                                _log.warn(_loc.get("drop-fk", fks[k], tabs[j]));
                    }
                }
            }

            // also drop imported foreign keys for tables that will be dropped
            Table tab;
            for (Iterator<Table> itr = drops.iterator(); itr.hasNext();) {
                tab = itr.next();
                dbTable = db.findTable(tab);
                if (dbTable == null)
                    continue;

                fks = db.findExportedForeignKeys(dbTable.getPrimaryKey());
                for (int i = 0; i < fks.length; i++) {
                    if (dropForeignKey(fks[i]))
                        dbTable.removeForeignKey(fks[i]);
                    else
                        _log.warn(_loc.get("drop-fk", fks[i], dbTable));
                }
            }
        }

        // drop the tables we calculated above
        dropTables(drops, db);

        // columns
        Column[] cols;
        Column col;
        for (int i = 0; i < schemas.length; i++) {
            tabs = schemas[i].getTables();
            for (int j = 0; j < tabs.length; j++) {
                if (!isDroppable(tabs[j]))
                    continue;
                cols = tabs[j].getColumns();
                dbTable = db.findTable(tabs[j]);
                for (int k = 0; k < cols.length; k++) {
                    col = null;
                    if (dbTable != null)
                        col = dbTable.getColumn(cols[k].getIdentifier());
                    if (dbTable == null || col == null)
                        continue;

                    if (dropColumn(cols[k])) {
                        if (dbTable != null)
                            dbTable.removeColumn(col);
                        else
                            _log.warn(_loc.get("drop-col", cols[k], tabs[j]));
                    }
                }
            }
        }
    }

    /**
     * Return true if the table is droppable.
     */
    protected boolean isDroppable(Table table) {
        return _openjpaTables
            || (!DBIdentifier.toUpper(table.getIdentifier()).getName().startsWith("OPENJPA_")
            && !DBIdentifier.toUpper(table.getIdentifier()).getName().startsWith("JDO_")); // legacy
    }

    /**
     * Return true if the sequence is droppable.
     */
    protected boolean isDroppable(Sequence seq) {
        return _openjpaTables
            || (!DBIdentifier.toUpper(seq.getIdentifier()).getName().startsWith("OPENJPA_")
            && !DBIdentifier.toUpper(seq.getIdentifier()).getName().startsWith("JDO_")); // legacy
    }

    /**
     * Find an index in the given table that matches the given one.
     */
    protected Index findIndex(Table dbTable, Index idx) {
        Index[] idxs = dbTable.getIndexes();
        for (int i = 0; i < idxs.length; i++)
            if (idx.columnsMatch(idxs[i].getColumns()))
                return idxs[i];
        return null;
    }

    /**
     * Find a foreign key in the given table that matches the given one.
     */
    protected ForeignKey findForeignKey(Table dbTable, ForeignKey fk) {
        if (fk.getConstantColumns().length > 0
            || fk.getConstantPrimaryKeyColumns().length > 0)
            return null;
        ForeignKey[] fks = dbTable.getForeignKeys();
        for (int i = 0; i < fks.length; i++)
            if (fk.columnsMatch(fks[i].getColumns(),
                fks[i].getPrimaryKeyColumns()))
                return fks[i];
        return null;
    }

    /**
     * Remove the given collection of tables from the database schema. Orders
     * the removals according to foreign key constraints on the tables.
     */
    protected void dropTables(Collection<Table> tables, SchemaGroup change)
        throws SQLException {
        if (tables.isEmpty())
            return;

        Table table;
        Table changeTable;
        for (Iterator<Table> itr = tables.iterator(); itr.hasNext();) {
            table = itr.next();
            if (dropTable(table)) {
                changeTable = change.findTable(table);
                if (changeTable != null)
                    changeTable.getSchema().removeTable(changeTable);
            } else
                _log.warn(_loc.get("drop-table", table));
        }
    }

    /**
     * Add the given table to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean createTable(Table table)
        throws SQLException {
        return executeSQL(_dict.getCreateTableSQL(table, _db));
    }

    /**
     * Drop the given table from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropTable(Table table)
        throws SQLException {
        return executeSQL(_dict.getDropTableSQL(table));
    }

    /**
     * Add the given sequence to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean createSequence(Sequence seq)
        throws SQLException {
        return executeSQL(_dict.getCreateSequenceSQL(seq));
    }

    /**
     * Drop the given sequence from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropSequence(Sequence seq)
        throws SQLException {
        return executeSQL(_dict.getDropSequenceSQL(seq));
    }

    /**
     * Add the given index to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean createIndex(Index idx, Table table)
        throws SQLException {
        return createIndex(idx, table, null);
    }
    
    public boolean createIndex(Index idx, Table table, Unique[] uniques)
        throws SQLException {
        // Informix will automatically create a unique index for the 
        // primary key, so don't create another index again

        if (!_dict.needsToCreateIndex(idx,table,uniques))
            return false;

        int max = _dict.maxIndexesPerTable;

        int len = table.getIndexes().length;
        if (table.getPrimaryKey() != null)
            len += table.getPrimaryKey().getColumns().length;

        if (len >= max) {
            _log.warn(_loc.get("too-many-indexes", idx, table, max + ""));
            return false;
        }

        return executeSQL(_dict.getCreateIndexSQL(idx));
    }

    /**
     * Drop the given index from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropIndex(Index idx)
        throws SQLException {
        return executeSQL(_dict.getDropIndexSQL(idx));
    }

    /**
     * Add the given column to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean addColumn(Column col)
        throws SQLException {
        return executeSQL(_dict.getAddColumnSQL(col));
    }

    /**
     * Drop the given column from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropColumn(Column col)
        throws SQLException {
        return executeSQL(_dict.getDropColumnSQL(col));
    }

    /**
     * Add the given primary key to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean addPrimaryKey(PrimaryKey pk)
        throws SQLException {
        return executeSQL(_dict.getAddPrimaryKeySQL(pk));
    }

    /**
     * Drop the given primary key from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropPrimaryKey(PrimaryKey pk)
        throws SQLException {
        return executeSQL(_dict.getDropPrimaryKeySQL(pk));
    }

    /**
     * Add the given foreign key to the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean addForeignKey(ForeignKey fk)
        throws SQLException {
        return executeSQL(_dict.getAddForeignKeySQL(fk));
    }

    /**
     * Drop the given foreign key from the database schema.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean dropForeignKey(ForeignKey fk)
        throws SQLException {
        Connection conn = _ds.getConnection();
        try {
            return executeSQL(_dict.getDropForeignKeySQL(fk,conn));
        } finally {
            closeConnection(conn);
        }

    }

    /**
     * Return the database schema.
     */
    public SchemaGroup getDBSchemaGroup() {
        try {
            return getDBSchemaGroup(true);
        } catch (SQLException se) {
            throw SQLExceptions.getStore(se, _dict);
        }
    }

    /**
     * Set the database schema.
     */
    public void setDBSchemaGroup(SchemaGroup db) {
        _db = db;
        if (db != null)
            _fullDB = true;
    }

    /**
     * Return the database schema.
     *
     * @param full if false, only the tables named in the set schema
     * repository will be generated
     */
    protected SchemaGroup getDBSchemaGroup(boolean full)
        throws SQLException {
        if (_db == null || (full && !_fullDB)) {
            SchemaGenerator gen = new SchemaGenerator(_conf);
            gen.setPrimaryKeys(_pks);
            gen.setForeignKeys(_fks);
            gen.setIndexes(_indexes);
            if (full)
                gen.generateSchemas();
            else {
                // generate only the tables in the given repository
                // group; some may not exist yet, which is OK; we just need
                // to make sure we can detect the changes to the ones that
                // do exist
                Collection<DBIdentifier> tables = new LinkedList<DBIdentifier>();
                SchemaGroup group = assertSchemaGroup();
                Schema[] schemas = group.getSchemas();
                Table[] tabs;
                for (int i = 0; i < schemas.length; i++) {
                    tabs = schemas[i].getTables();
                    for (int j = 0; j < tabs.length; j++) {
                        if (DBIdentifier.isNull(tabs[j].getSchemaIdentifier())) {
                            tables.add(tabs[j].getIdentifier());
                        } else {
                            DBIdentifier sName = tabs[j].getFullIdentifier();
                            tables.add(sName);
                        }
                    }
                }
                if (!tables.isEmpty())
                    gen.generateSchemas((DBIdentifier[]) tables.toArray
                        (new DBIdentifier[tables.size()]));
            }
            _db = gen.getSchemaGroup();
        }
        return _db;
    }

    protected SchemaGroup assertSchemaGroup() {
        SchemaGroup local = getSchemaGroup();
        if (local == null)
            throw new InvalidStateException(_loc.get("tool-norepos"));
        return local;
    }

    /////////////
    // Utilities
    /////////////

    /**
     * Executes the given array of non-selecting SQL statements, correctly
     * logging the SQL calls and optionally ignoring errors.
     *
     * @return true if there was SQL to execute and the calls were
     * successful, false otherwise
     */
    protected boolean executeSQL(String[] sql)
        throws SQLException {
        // if no sql, probably b/c dictionary doesn't support operation
        if (sql.length == 0)
            return false;

        boolean err = false;
        if (_writer == null) {
            // this is outside the try-catch because a failure here is
            // really bad, and should not be ignored.
            Connection conn = _ds.getConnection();
            Statement statement = null;
            boolean wasAuto = true;
            try {
                wasAuto = conn.getAutoCommit();
                if (!wasAuto)
                    conn.setAutoCommit(true);
                for (int i = 0; i < sql.length; i++) {
                    try {
                        // some connections require that rollback be
                        // called on the connection before any DDL statements
                        // can be run on it, even when autocommit is on.
                        // This is sometimes because the connection does not
                        // allow DDL statements when there are multiple
                        // commands issued on the connection, and the
                        // connection pool may have issued some validation SQL.
                        try {
                            conn.rollback();
                        } catch (Exception e) {
                        }

                        statement = conn.createStatement();
                        statement.executeUpdate(sql[i]);

                        // some connections seem to require an explicit
                        // commit for DDL statements, even when autocommit
                        // is on. The DataDirect drivers seem to suffer from
                        // this limitation.
                        try {
                            conn.commit();
                        } catch (Exception e) {
                        }
                    }
                    catch (SQLException se) {
                        err = true;
                        handleException(se);
                    } finally {
                        if (statement != null)
                            try {
                                statement.close();
                            } catch (SQLException se) {
                            }
                    }
                }
            }
            finally {
                if (!wasAuto) {
                    conn.setAutoCommit(false);
                }

                try {
                    closeConnection(conn);
                } catch (SQLException se) {
                    //X TODO why catch silently?
                }
            }
        } else {
            for (int i = 0; i < sql.length; i++)
                _writer.println(sql[i] + _sqlTerminator);
            _writer.flush();
        }

        return !err;
    }

    /**
     * Handle the given exception, logging it and optionally ignoring it,
     * depending on the flags this SchemaTool was created with.
     */
    protected void handleException(SQLException sql)
        throws SQLException {
        if (!_ignoreErrs)
            throw sql;
        _log.warn(sql.getMessage(), sql);
    }

    ////////
    // Main
    ////////

    /**
     * Usage: java org.apache.openjpa.jdbc.schema.SchemaTool [option]*
     * [-action/-a &lt;add | retain | drop | refresh | createDB | dropDB
     * | build | reflect | import | export&gt;]
     * &lt;.schema file or resource&gt;*
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The
     * path or resource name of a OpenJPA properties file containing
     * information such as the license key	and connection data as
     * outlined in {@link JDBCConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link JDBCConfiguration} can be set by
     * using their	names and supplying a value. For example:
     * <code>-licenseKey adslfja83r3lkadf</code></li>
     * <li><i>-ignoreErrors/-i &lt;true/t | false/f&gt;</i>: If false, an
     * exception will will be thrown if the tool encounters any database
     * exceptions; defaults to <code>false</code>.</li>
     * <li><i>-file/-f &lt;stdout | output file or resource&gt;</i>: Use this
     * option to write a SQL script for the planned schema modifications,
     * rather than committing them to the database. This option also
     * applies to the <code>export</code> and <code>reflect</code> actions.</li>
     * <li><i>-openjpaTables/-kt &lt;true/t | false/f&gt;</i>: Under the
     * <code>reflect</code> action, whether to reflect on tables with
     * the name <code>OPENJPA_*</code>. Under other actions, whether to
     * drop such tables. Defaults to <code>false</code>.</li>
     * <li><i>-dropTables/-dt &lt;true/t | false/f&gt;</i>: Set this option to
     * true to drop tables that appear to be unused during
     * <code>retain</code>	and <code>refresh</code> actions. Defaults to
     * <code>true</code>.</li>
     * <li><i>-dropSequences/-dsq &lt;true/t | false/f&gt;</i>: Set this option
     * to true to drop sequences that appear to be unused during
     * <code>retain</code>	and <code>refresh</code> actions. Defaults to
     * <code>true</code>.</li>
     * <li><i>-primaryKeys/-pk &lt;true/t | false/f&gt;</i>: Whether primary
     * keys on existing tables are manipulated. Defaults to true.</li>
     * <li><i>-foreignKeys/-fk &lt;true/t | false/f&gt;</i>: Whether foreign
     * keys on existing tables are manipulated. Defaults to true.</li>
     * <li><i>-indexes/-ix &lt;true/t | false/f&gt;</i>: Whether indexes
     * on existing tables are manipulated. Defaults to true.</li>
     * <li><i>-sequences/-sq &lt;true/t | false/f&gt;</i>: Whether to
     * manipulate sequences. Defaults to true.</li>
     * <li><i>-record/-r &lt;true/t | false/f&gt;</i>: Set this option to
     * <code>false</code> to prevent writing the schema changes to the
     * current {@link SchemaFactory}.</li>
     * </ul>
     *  Actions can be composed in a comma-separated list. The various actions 
     *  are as follows.
     * <ul>
     * <li><i>add</i>: Bring the schema up-to-date with the latest
     * changes to the schema XML data by adding tables, columns,
     * indexes, etc. This action never drops any data. This is the
     * default action.</li>
     * <li><i>retain</i>: Keep all schema components in the schema XML, but
     * drop the rest from the database. This action never adds any data.</li>
     * <li><i>drop</i>: Drop all the schema components in the schema XML.</li>
     * <li><i>refresh</i>: Equivalent to retain, then add.</li>
     * <li><i>createDB</i>: Execute SQL to re-create the current database.
     * This action is typically used in conjuction with the
     * <code>file</code> option.</li>
     * <li><i>build</i>: Execute SQL to build the schema defined in the XML.
     * Because it doesn't take the current database schema into account,
     * this action is typically used in conjuction with the
     * <code>file</code> option.</li>
     * <li><i>reflect</i>: Reflect on the current database schema. Write the
     * schema's XML representation to the file specified with the
     * <code>file</code> option, or to stdout if no file is given.</li>
     * <li><i>dropDB</i>: Execute SQL to drop the current database. This
     * action implies <code>dropTables</code>.</li>
     * <li><i>deleteTableContents</i>: Execute SQL to delete all rows from 
     * all tables that OpenJPA knows about.</li>
     * <li><i>import</i>: Import the given XML schema definition into the
     * current {@link SchemaFactory}.</li>
     * <li><i>export</i>: Export the current {@link SchemaFactory}'s recorded
     * schema to an XML schema definition file.</li>
     * </ul>
     *  Examples:
     * <ul>
     * <li>Write a script to stdout to re-create the current database
     * schema:<br />
     * <code>java org.apache.openjpa.jdbc.schema.SchemaTool -f stdout 
     * -a createDB</code></li>
     * <li>Drop the current database schema:<br />
     * <code>java org.apache.openjpa.jdbc.schema.SchemaTool 
     * -a dropDB</code></li>
     * <li>Refresh the schema and delete all records in all tables:<br />
     * <code>java org.apache.openjpa.jdbc.schema.SchemaTool 
     * -a refresh,deleteTableContents</code></li>
     * <li>Create a schema based on an XML schema definition file:<br />
     * <code>java org.apache.openjpa.jdbc.schema.SchemaTool 
     * myschema.xml</code></li>
     * </ul>
     */
    public static void main(String[] args)
        throws IOException, SQLException {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws Exception {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                try {
                    return SchemaTool.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.out.println(_loc.get("tool-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if any invalid options were given.
     *
     * @see #main
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts)
        throws IOException, SQLException {
        Flags flags = new Flags();
        flags.dropTables = opts.removeBooleanProperty
            ("dropTables", "dt", flags.dropTables);
        flags.dropSequences = opts.removeBooleanProperty
            ("dropSequences", "dsq", flags.dropSequences);
        flags.ignoreErrors = opts.removeBooleanProperty
            ("ignoreErrors", "i", flags.ignoreErrors);
        flags.openjpaTables = opts.removeBooleanProperty
            ("openjpaTables", "ot", flags.openjpaTables);
        flags.primaryKeys = opts.removeBooleanProperty
            ("primaryKeys", "pk", flags.primaryKeys);
        flags.foreignKeys = opts.removeBooleanProperty
            ("foreignKeys", "fks", flags.foreignKeys);
        flags.indexes = opts.removeBooleanProperty
            ("indexes", "ix", flags.indexes);
        flags.sequences = opts.removeBooleanProperty
            ("sequences", "sq", flags.sequences);
        flags.record = opts.removeBooleanProperty("record", "r", flags.record);
        String fileName = opts.removeProperty("file", "f", null);
        String schemas = opts.removeProperty("s");
        if (schemas != null)
            opts.setProperty("schemas", schemas);

        String[] actions = opts.removeProperty("action", "a", flags.action)
            .split(",");
        
        // setup a configuration instance with cmd-line info
        Configurations.populateConfiguration(conf, opts);

        // create script writer
        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(SchemaTool.class, null);
        flags.writer = Files.getWriter(fileName, loader);

        boolean returnValue = true;
        for (int i = 0; i < actions.length; i++) {
            flags.action = actions[i];
            returnValue &= run(conf, args, flags, loader);
        }
        
        return returnValue;
    }

    /**
     * Run the tool. Return false if invalid options were given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Flags flags, ClassLoader loader)
        throws IOException, SQLException {
        Log log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
        if (ACTION_REFLECT.equals(flags.action)) {
            if (args.length > 0)
                return false;
            if (flags.writer == null)
                flags.writer = new PrintWriter(System.out);

            SchemaGenerator gen = new SchemaGenerator(conf);
            gen.setPrimaryKeys(flags.primaryKeys);
            gen.setIndexes(flags.indexes);
            gen.setForeignKeys(flags.foreignKeys);
            gen.setSequences(flags.sequences);
            gen.setOpenJPATables(flags.openjpaTables);

            String schemas = conf.getSchemas();
            if (StringUtils.isEmpty(schemas))
                schemas = "all";
            log.info(_loc.get("sch-reflect", schemas));
            gen.generateSchemas();

            // record the schema
            log.info(_loc.get("sch-reflect-write"));
            SchemaSerializer ser = new XMLSchemaSerializer(conf);
            ser.addAll(gen.getSchemaGroup());
            ser.serialize(flags.writer, MetaDataSerializer.PRETTY);
            return true;
        }

        if (args.length == 0
            && !ACTION_CREATEDB.equals(flags.action)
            && !ACTION_DROPDB.equals(flags.action)
            && !ACTION_EXPORT.equals(flags.action)
            && !ACTION_DELETE_TABLE_CONTENTS.equals(flags.action))
            return false;

        // parse in the arguments
        SchemaParser parser = new XMLSchemaParser(conf);
        parser.setDelayConstraintResolve(true);
        File file;
        for (int i = 0; i < args.length; i++) {
            file = Files.getFile(args[i], loader);
            log.info(_loc.get("tool-running", file));
            parser.parse(file);
        }
        parser.resolveConstraints();

        if (ACTION_IMPORT.equals(flags.action)) {
            log.info(_loc.get("tool-import-store"));
            SchemaGroup schema = parser.getSchemaGroup();
            conf.getSchemaFactoryInstance().storeSchema(schema);
            return true;
        }
        if (ACTION_EXPORT.equals(flags.action)) {
            if (flags.writer == null)
                flags.writer = new PrintWriter(System.out);

            log.info(_loc.get("tool-export-gen"));
            SchemaGroup schema = conf.getSchemaFactoryInstance().readSchema();

            log.info(_loc.get("tool-export-write"));
            SchemaSerializer ser = new XMLSchemaSerializer(conf);
            ser.addAll(schema);
            ser.serialize(flags.writer, MetaDataSerializer.PRETTY);
            return true;
        }

        SchemaTool tool = new SchemaTool(conf, flags.action);
        tool.setIgnoreErrors(flags.ignoreErrors);
        tool.setDropTables(flags.dropTables);
        tool.setSequences(flags.sequences); // set before dropseqs
        tool.setDropSequences(flags.dropSequences);
        tool.setPrimaryKeys(flags.primaryKeys);
        tool.setForeignKeys(flags.foreignKeys);
        tool.setIndexes(flags.indexes);
        tool.setOpenJPATables(flags.openjpaTables);
        if (args.length > 0)
            tool.setSchemaGroup(parser.getSchemaGroup());
        if (flags.writer != null)
            tool.setWriter(flags.writer);

        log.info(_loc.get("tool-action", flags.action));
        try {
            tool.run();
        } finally {
            if (flags.record) {
                log.info(_loc.get("tool-record"));
                tool.record();
            }
        }
        if (flags.writer != null)
            flags.writer.flush();

        return true;
    }

    private void closeConnection(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    /**
     * Run flags.
     */
    public static class Flags {

        public String action = ACTION_ADD;
        public Writer writer = null;
        public boolean dropTables = true;
        public boolean dropSequences = true;
        public boolean ignoreErrors = false;
        public boolean openjpaTables = false;
        public boolean primaryKeys = true;
        public boolean foreignKeys = true;
        public boolean indexes = true;
        public boolean sequences = true;
        public boolean record = true;
    }
}
