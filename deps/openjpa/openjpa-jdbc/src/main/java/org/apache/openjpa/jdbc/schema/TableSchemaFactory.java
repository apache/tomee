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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.MetaDataSerializer;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.GeneralException;

/**
 * Factory that uses an XML schema definition stored in a database table
 * to record the system schema.
 *
 * @author Abe White
 */
public class TableSchemaFactory
    implements SchemaFactory, Configurable {

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DROP = "drop";

    private static final Localizer _loc = Localizer.forPackage
        (TableSchemaFactory.class);
    private static boolean _refreshedTable = false;

    private JDBCConfiguration _conf = null;
    private Log _log = null;
    private DBIdentifier _table = DBIdentifier.newTable("OPENJPA_SCHEMA");
    private DBIdentifier _pkColumnName = DBIdentifier.newColumn("ID");
    private DBIdentifier _schemaColumnName = DBIdentifier.newColumn("SCHEMA_DEF");
    private Column _pkColumn = null;
    private Column _schemaColumn = null;

    /**
     * The name of the schema definition table. Defaults to
     * <code>OPENJPA_SCHEMA</code>.
     */
    public String getTable() {
        return _table.getName();
    }

    /**
     * The name of the schema definition table. Defaults to
     * <code>OPENJPA_SCHEMA</code>.
     */
    public void setTable(String name) {
        _table = DBIdentifier.newTable(name);
    }

    /**
     * @deprecated Use {@link #setTable}. Retained for
     * backwards-compatible auto-configuration.
     */
    public void setTableName(String name) {
        setTable(name);
    }

    /**
     * The name of the primary key column on the schema definition table.
     * Defaults to <code>ID</code>.
     */
    public void setPrimaryKeyColumn(String name) {
        _pkColumnName = DBIdentifier.newColumn(name);
    }

    /**
     * The name of the primary key column on the schema definition table.
     * Defaults to <code>ID</code>.
     */
    public String getPrimaryKeyColumn() {
        return _pkColumnName.getName();
    }

    /**
     * The name of the schema column on the schema definition table.
     * Defaults to <code>SCHEMA_DEF</code>.
     */
    public void setSchemaColumn(String name) {
        _schemaColumnName = DBIdentifier.newColumn(name);
    }

    /**
     * The name of the schema column on the schema definition table.
     * Defaults to <code>SCHEMA_DEF</code>.
     */
    public String getSchemaColumn() {
        return _schemaColumnName.getName();
    }

    public JDBCConfiguration getConfiguration() {
        return _conf;
    }

    public void setConfiguration(Configuration conf) {
        _conf = (JDBCConfiguration) conf;
        _log = _conf.getLog(JDBCConfiguration.LOG_SCHEMA);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        buildTable();
    }
    
    public synchronized SchemaGroup readSchema() {
        String schema = null;
        try {
            schema = readSchemaColumn();
        } catch (SQLException se) {
            if (_log.isWarnEnabled())
                _log.warn(_loc.get("bad-sch-read", se));
        }
        if (schema == null)
            return new SchemaGroup();

        XMLSchemaParser parser = new XMLSchemaParser(_conf);
        try {
            parser.parse(new StringReader(schema),
                _schemaColumn.getQualifiedPath().toString());
        } catch (IOException ioe) {
            throw new GeneralException(ioe);
        }
        return parser.getSchemaGroup();
    }

    public void storeSchema(SchemaGroup schema) {
        XMLSchemaSerializer ser = new XMLSchemaSerializer(_conf);
        ser.addAll(schema);
        Writer writer = new StringWriter();
        try {
            ser.serialize(writer, MetaDataSerializer.COMPACT);
        } catch (IOException ioe) {
            throw new GeneralException(ioe);
        }

        String schemaStr = writer.toString();
        try {
            writeSchemaColumn(schemaStr);
        } catch (SQLException se) {
            if (_log.isWarnEnabled())
                _log.warn(_loc.get("bad-sch-write-1", se));

            // maybe the update failed b/c the sequence table doesn't
            // exist yet; create it now; note that though we synchronize
            // here, other JVMs can still be trying to create the table at
            // the same time (unlikely, since this is a dev-tool op)
            synchronized (TableSchemaFactory.class) {
                if (!_refreshedTable) {
                    _refreshedTable = true;
                    try {
                        refreshTable();
                    } catch (Exception e) {
                        if (_log.isWarnEnabled())
                            _log.warn(_loc.get("bad-sch-ref", e));
                    }
                }

                try {
                    writeSchemaColumn(schemaStr);
                } catch (Exception e) {
                    if (_log.isWarnEnabled())
                        _log.warn(_loc.get("bad-sch-write-2"));

                    // throw original exception
                    throw SQLExceptions.getStore(se,
                        _conf.getDBDictionaryInstance());
                }
            }
        }
    }

    /**
     * Creates the schema table in the DB.
     */
    public void refreshTable()
        throws SQLException {
        if (_log.isInfoEnabled())
            _log.info(_loc.get("make-sch-table"));

        // create the table
        SchemaTool tool = new SchemaTool(_conf);
        tool.setIgnoreErrors(true);
        tool.createTable(_pkColumn.getTable());

        // insert an empty schema
        Connection conn = getConnection();
        PreparedStatement stmnt = null;
        boolean wasAuto = true;
        try {
            wasAuto = conn.getAutoCommit();
            if (!wasAuto)
                conn.setAutoCommit(true);

            DBDictionary dict = _conf.getDBDictionaryInstance();
            stmnt = conn.prepareStatement("INSERT INTO "
                + dict.getFullName(_pkColumn.getTable(), false)
                + " (" + dict.getColumnDBName(_pkColumn) + ", " + 
                dict.getColumnDBName(_schemaColumn) + ") VALUES (?, ?)");
            dict.setInt(stmnt, 1, 1, _pkColumn);
            dict.setNull(stmnt, 2, _schemaColumn.getType(), _schemaColumn);
            dict.setTimeouts(stmnt, _conf, true);
            stmnt.executeUpdate();
        } finally {
            if (stmnt != null)
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            if (!wasAuto)
                conn.setAutoCommit(false);
            try {
                conn.close();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Drops the sequence table in the DB.
     */
    public void dropTable()
        throws SQLException {
        if (_log.isInfoEnabled())
            _log.info(_loc.get("drop-sch-table"));

        // drop the table
        SchemaTool tool = new SchemaTool(_conf);
        tool.setIgnoreErrors(true);
        tool.dropTable(_pkColumn.getTable());
    }

    /**
     * Returns the schema as an XML string.
     */
    public String readSchemaColumn()
        throws SQLException {
        DBDictionary dict = _conf.getDBDictionaryInstance();
        SQLBuffer sel = new SQLBuffer(dict).append(_schemaColumn);
        SQLBuffer where = new SQLBuffer(dict).append(_pkColumn).append(" = ").
            appendValue(1, _pkColumn);
        SQLBuffer tables = new SQLBuffer(dict).append(_pkColumn.getTable());

        SQLBuffer select = dict.toSelect(sel, null, tables, where, null,
            null, null, false, false, 0, Long.MAX_VALUE);

        Connection conn = getConnection();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        boolean wasAuto = true;
        try {
            wasAuto = conn.getAutoCommit();
            if (!wasAuto)
                conn.setAutoCommit(true);

            stmnt = select.prepareStatement(conn);
            dict.setQueryTimeout(stmnt, _conf.getQueryTimeout());
            rs = stmnt.executeQuery();
            rs.next();
            String schema = (_schemaColumn.getType() == Types.CLOB) ?
                dict.getClobString(rs, 1) : dict.getString(rs, 1);
            return schema;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException se) {
                }
            if (stmnt != null)
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            if (!wasAuto)
                conn.setAutoCommit(false);
            try {
                conn.close();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Writes the schema as a string to the database.
     */
    public void writeSchemaColumn(String schema)
        throws SQLException {
        // always use special clob handling when dict has max embedded size;
        // for some reason optimizing for string length causes errors
        DBDictionary dict = _conf.getDBDictionaryInstance();
        boolean embedded = dict.maxEmbeddedClobSize == -1;
        String update;
        if (embedded)
            update = "UPDATE " + dict.getFullName(_pkColumn.getTable(), false)
                + " SET " + dict.getColumnDBName(_schemaColumn) + " = ?  WHERE " +
                dict.getColumnIdentifier(_pkColumn) + " = ?";
        else
            update = "SELECT " + dict.getColumnDBName(_schemaColumn) + " FROM "
                + dict.getFullName(_pkColumn.getTable(), false)
                + " WHERE " + dict.getColumnDBName(_pkColumn) + " = ?";

        Connection conn = getConnection();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        boolean wasAuto = true;
        try {
            // if embedded we want autocommit true, else false
            wasAuto = conn.getAutoCommit();
            if (wasAuto != embedded)
                conn.setAutoCommit(embedded);

            if (embedded) {
                stmnt = conn.prepareStatement(update);
                if (schema == null)
                    dict.setNull(stmnt, 1, _schemaColumn.getType(),
                        _schemaColumn);
                else if (_schemaColumn.getType() == Types.CLOB)
                    dict.setClobString(stmnt, 1, schema, _schemaColumn);
                else
                    dict.setString(stmnt, 1, schema, _schemaColumn);
                dict.setInt(stmnt, 2, 1, _pkColumn);
                dict.setTimeouts(stmnt, _conf, true);
                stmnt.executeUpdate();
            } else {
                stmnt = conn.prepareStatement(update,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
                dict.setInt(stmnt, 1, 1, _pkColumn);
                dict.setTimeouts(stmnt, _conf, true);
                rs = stmnt.executeQuery();
                rs.next();
                dict.putString(rs.getClob(1), schema);
                conn.commit();
            }
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException se) {
                }
            if (stmnt != null)
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            if (wasAuto != embedded)
                conn.setAutoCommit(wasAuto);
            try {
                conn.close();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Creates the object-level representation of the sequence table.
     */
    private void buildTable() {
        QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(_table);
        DBIdentifier tableName = path.getIdentifier();
        DBIdentifier schemaName = path.getSchemaName();
        if (DBIdentifier.isEmpty(schemaName))
            schemaName = Schemas.getNewTableSchemaIdentifier(_conf);

        // build the table in one of the designated schemas
        SchemaGroup group = new SchemaGroup();
        Schema schema = group.addSchema(schemaName);

        Table table = schema.addTable(tableName);
        PrimaryKey pk = table.addPrimaryKey();

        DBDictionary dict = _conf.getDBDictionaryInstance();
        _pkColumn = table.addColumn(dict.getValidColumnName
            (_pkColumnName, table));
        _pkColumn.setType(dict.getPreferredType(Types.TINYINT));
        _pkColumn.setJavaType(JavaTypes.INT);
        pk.addColumn(_pkColumn);

        _schemaColumn = table.addColumn(dict.getValidColumnName
            (_schemaColumnName, table));
        _schemaColumn.setType(dict.getPreferredType(Types.CLOB));
        _schemaColumn.setJavaType(JavaTypes.STRING);
    }

    /**
     * Return a connection to use.
     */
    private Connection getConnection()
        throws SQLException {
        return _conf.getDataSource2(null).getConnection();
    }

    /////////
    // Main
    /////////

    /**
     * Usage: java org.apache.openjpa.jdbc.schema.TableSchemaFactory
     * [option]* -action/-a &lt;add | drop&gt;
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
     * </ul>
     *  The various actions are as follows.
     * <ul>
     * <li><i>add</i>: Create the schema table.</li>
     * <li><i>drop</i>: Drop the schema table.</li>
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
                    return TableSchemaFactory.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.out.println(_loc.get("sch-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if invalid options were given.
     *
     * @see #main
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts)
        throws IOException, SQLException {
        String action = opts.removeProperty("action", "a", null);
        Configurations.populateConfiguration(conf, opts);
        return run(conf, action);
    }

    /**
     * Run the tool.
     */
    public static boolean run(JDBCConfiguration conf, String action)
        throws IOException, SQLException {
        // run the action
        TableSchemaFactory factory = new TableSchemaFactory();
        String props = Configurations.getProperties(conf.getSchemaFactory());
        Configurations.configureInstance(factory, conf, props);

        if (ACTION_DROP.equals(action))
            factory.dropTable();
        else if (ACTION_ADD.equals(action))
            factory.refreshTable();
        else
            return false;
        return true;
    }
}
