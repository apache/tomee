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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.SchemaTool;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;

///////////////////////////////////////////////////////////
// NOTE: Do not change property names; see SequenceMetaData
// and SequenceMapping for standard property names.
////////////////////////////////////////////////////////////

/**
 * {@link JDBCSeq} implementation that uses a database sequence
 * to generate numbers.
 * Supports allocation (caching). In order for allocation to work properly, the database sequence must be defined
 * with INCREMENT BY value equal to allocate * increment.
 *
 * @see JDBCSeq
 * @see AbstractJDBCSeq
 */
public class NativeJDBCSeq
    extends AbstractJDBCSeq
    implements Configurable {

    public static final String ACTION_DROP = "drop";
    public static final String ACTION_ADD = "add";
    public static final String ACTION_GET = "get";

    private static Localizer _loc = Localizer.forPackage(NativeJDBCSeq.class);

    private JDBCConfiguration _conf = null;
    private DBIdentifier _seqName = DBIdentifier.newSequence("OPENJPA_SEQUENCE");
    private int _increment = 1;
    private int _initial = 1;
    private int _allocate = 50;
    private Sequence _seq = null;
    private String _select = null;
    private long _nextValue = 0;
    private long _maxValue = -1;

    private DBIdentifier _schema = DBIdentifier.NULL;

    private boolean alterIncrementBy = false;
    private boolean alreadyLoggedAlterSeqFailure = false;

    /**
     * The sequence name. Defaults to <code>OPENJPA_SEQUENCE</code>.
     */
    // @GETTER
    public String getSequence() {
        return _seqName.getName();
    }

    /**
     * The sequence name. Defaults to <code>OPENJPA_SEQUENCE</code>.
     */
    public void setSequence(String seqName) {
        _seqName = DBIdentifier.newSequence(seqName);
    }

    /**
     * @see Sequence#getInitialValue
     */
    public int getInitialValue() {
        return _initial;
    }

    /**
     * @see Sequence#setInitialValue
     */
    public void setInitialValue(int initial) {
        _initial = initial;
    }

    /**
     * @see Sequence#getAllocate
     */
    public int getAllocate() {
        return _allocate;
    }

    /**
     * @see Sequence#setAllocate
     */
    public void setAllocate(int allocate) {
        _allocate = allocate;
    }

    /**
     * @see Sequence#getIncrement
     */
    public int getIncrement() {
        return _increment;
    }

    /**
     * @see Sequence#setIncrement
     */
    public void setIncrement(int increment) {
        _increment = increment;
    }

    @Override
    public void addSchema(ClassMapping mapping, SchemaGroup group) {
        // sequence already exists?
        QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(_seqName);
        if (group.isKnownSequence(path))
            return;

        DBIdentifier schemaName = getSchemaIdentifier();
        if (DBIdentifier.isEmpty(schemaName)) {
            schemaName = path.getSchemaName();
            if (DBIdentifier.isEmpty(schemaName))
                schemaName = Schemas.getNewTableSchemaIdentifier(_conf);
        }

        // create table in this group
        Schema schema = group.getSchema(schemaName);
        if (schema == null)
            schema = group.addSchema(schemaName);
        schema.importSequence(_seq);
    }

    @Override
    public JDBCConfiguration getConfiguration() {
        return _conf;
    }
    
    public void setConfiguration(Configuration conf) {
        _conf = (JDBCConfiguration) conf;
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        buildSequence();

        DBDictionary dict = _conf.getDBDictionaryInstance();
        String format = dict.nextSequenceQuery;
        if (format == null) {
            throw new MetaDataException(_loc.get("no-seq-sql", _seqName));
        }

        String name = dict.getFullName(_seq);
        // Increment step is needed for Firebird which uses non-standard sequence fetch syntax.
        // Use String.valueOf to get rid of possible locale-specific number formatting.
        _select = MessageFormat.format(format, new Object[]{name, String.valueOf(_allocate * _increment)});
        
        type = dict.nativeSequenceType;
    }
    
    @Override
    protected synchronized Object nextInternal(JDBCStore store, ClassMapping mapping)
        throws SQLException {
        if (!alterIncrementBy) {
            allocateInternal(0, store, mapping);
            alterIncrementBy = true;
        }
        if (_nextValue >= _maxValue) {
            allocateInternal(0, store, mapping);
        }
        long result = _nextValue;
        _nextValue += _increment;
        return result;
    }

    /**
     * Allocate additional sequence values.
     * @param additional ignored - the allocation size is fixed and determined by allocate and increment properties. 
     * @param store used to obtain connection
     * @param mapping ignored
     */
    @Override
    protected synchronized void allocateInternal(int additional, JDBCStore store, ClassMapping mapping)
        throws SQLException {
        Connection conn = getConnection(store);
        try {
            if (!alterIncrementBy) {
                DBDictionary dict = _conf.getDBDictionaryInstance();
                // If this fails, we will warn the user at most one time and set _allocated and _increment to 1 so
                // as to not potentially insert records ahead of what the database thinks is the next sequence value.
                if (updateSql(conn, dict.getAlterSequenceSQL(_seq)) == -1) {
                    if (!alreadyLoggedAlterSeqFailure) {
                        Log log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
                        if (log.isWarnEnabled()) {
                            log.warn(_loc.get("fallback-no-seq-cache", _seqName));
                        }
                    }
                    alreadyLoggedAlterSeqFailure = true;
                    _allocate = 1;
                }
            }
            _nextValue = getSequence(conn);
            _maxValue = _nextValue + _allocate * _increment;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Creates the sequence object.
     */
    private void buildSequence() {
        QualifiedDBIdentifier path = QualifiedDBIdentifier.getPath(_seqName);
        DBIdentifier seqName = path.getIdentifier();
        // JPA 2 added schema as a configurable attribute on  
        // sequence generator.  OpenJPA <= 1.x allowed this via
        // schema.sequence on the sequence name.  Specifying a schema
        // name on the annotation or in the orm will override the old 
        // behavior.
        DBIdentifier schemaName = _schema;
        if (DBIdentifier.isEmpty(schemaName)) {
            schemaName = path.getSchemaName();
            if (DBIdentifier.isEmpty(schemaName))
                schemaName = Schemas.getNewTableSchemaIdentifier(_conf);
        }

        // build the sequence in one of the designated schemas
        SchemaGroup group = new SchemaGroup();
        Schema schema = group.addSchema(schemaName);

        _seq = schema.addSequence(seqName);
        _seq.setInitialValue(_initial);
        _seq.setIncrement(_increment);
        _seq.setAllocate(_allocate);
    }

    /**
     * Creates the sequence in the DB.
     */
    public void refreshSequence()
        throws SQLException {
        Log log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
        if (log.isInfoEnabled())
            log.info(_loc.get("make-native-seq"));

        // create the sequence
        SchemaTool tool = new SchemaTool(_conf);
        tool.setIgnoreErrors(true);
        tool.createSequence(_seq);
    }

    /**
     * Drops the sequence in the DB.
     */
    public void dropSequence()
        throws SQLException {
        Log log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
        if (log.isInfoEnabled())
            log.info(_loc.get("drop-native-seq"));

        // drop the table
        SchemaTool tool = new SchemaTool(_conf);
        tool.setIgnoreErrors(true);
        tool.dropSequence(_seq);
    }

    /**
     * Return the next sequence value.
     */
    private long getSequence(Connection conn)
        throws SQLException {
        DBDictionary dict = _conf.getDBDictionaryInstance();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = conn.prepareStatement(_select);
            dict.setTimeouts(stmnt, _conf, false);
            rs = stmnt.executeQuery();
            if (rs.next())
                return rs.getLong(1);

            // no row !?
            throw new UserException(_loc.get("invalid-seq-sql", _select));
        } finally {
            // clean up our resources
            if (rs != null)
                try { rs.close(); } catch (SQLException se) {}
            if (stmnt != null)
                try { stmnt.close(); } catch (SQLException se) {}
        }
    }

    private int updateSql(Connection conn, String sql) throws SQLException {
        DBDictionary dict = _conf.getDBDictionaryInstance();
        PreparedStatement stmnt = null;
        int rc = -1;
        try {
            stmnt = conn.prepareStatement(sql);
            dict.setTimeouts(stmnt, _conf, false);
            rc = stmnt.executeUpdate();
        } catch (Exception e) {
            // tolerate exception when attempting to alter increment,
            // however, caller should check rc and not cache sequence values if rc != -1.
        } finally {
            // clean up our resources
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            }
        }
        return rc;
    }

    /////////
    // Main
    /////////

    /**
     * Usage: java org.apache.openjpa.jdbc.schema.NativeJDBCSequence [option]*
     * -action/-a &lt;add | drop | get&gt;
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The
     * path or resource name of an OpenJPA properties file containing
     * information such as connection data as
     * outlined in {@link JDBCConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link JDBCConfiguration} can be set by
     * using their names and supplying a value.</li>
     * </ul>
     *  The various actions are as follows.
     * <ul>
     * <li><i>add</i>: Create the sequence.</li>
     * <li><i>drop</i>: Drop the sequence.</li>
     * <li><i>get</i>: Print the next sequence value.</li>
     * </ul>
     */
    public static void main(String[] args)
        throws Exception {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws Exception {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                try {
                    return NativeJDBCSeq.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.out.println(_loc.get("native-seq-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if invalid options were given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts)
        throws Exception {
        String action = opts.removeProperty("action", "a", null);
        Configurations.populateConfiguration(conf, opts);
        return run(conf, args, action);
    }

    /**
     * Run the tool. Returns false if an invalid option was given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        String action)
        throws Exception {
        if (args.length != 0)
            return false;

        NativeJDBCSeq seq = new NativeJDBCSeq();
        String props = Configurations.getProperties(conf.getSequence());
        Configurations.configureInstance(seq, conf, props);

        if (ACTION_DROP.equals(action))
            seq.dropSequence();
        else if (ACTION_ADD.equals(action))
            seq.refreshSequence();
        else if (ACTION_GET.equals(action)) {
            Connection conn = conf.getDataSource2(null).getConnection();
            try {
                long cur = seq.getSequence(conn);
                // START - ALLOW PRINT STATEMENTS
                System.out.println(cur);
                // STOP - ALLOW PRINT STATEMENTS
            } finally {
                try { conn.close(); } catch (SQLException se) {}
            }
        } else
            return false;
        return true;
    }

    /**
     * @deprecated
     */
    public void setSchema(String schema) {
        _schema = DBIdentifier.newSchema(schema);
    }

    /**
     * @deprecated
     */
    public String getSchema() {
        return _schema.getName();
    }

    public DBIdentifier getSchemaIdentifier() {
        return _schema;
    }

}
