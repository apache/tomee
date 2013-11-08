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
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Specialization of the {@link TableJDBCSeq} that maintains a
 * separate sequence count per-class. Table name defaults to
 * <code>OPENJPA_SEQUENCES_TABLE</code>.
 *
 * @author Abe White
 */
public class ClassTableJDBCSeq 
    extends TableJDBCSeq {

    private static final Localizer _loc = Localizer.forPackage
        (ClassTableJDBCSeq.class);

    private final Map<String, Status> _stats = new HashMap<String, Status>();
    private boolean _ignore = false;
    private boolean _aliases = false;

    public ClassTableJDBCSeq() {
        setTable("OPENJPA_SEQUENCES_TABLE");
    }

    /**
     * Whether unmapped classes should be ignored as possible primary
     * key values in the table. Defaults to false.
     */
    public boolean getIgnoreUnmapped() {
        return _ignore;
    }

    /**
     * Whether unmapped classes should be ignored as possible primary
     * key values in the table. Defaults to false.
     */
    public void setIgnoreUnmapped(boolean ignore) {
        _ignore = ignore;
    }

    /**
     * @deprecated Use {@link #setIgnoreUnmapped}. Retained for
     * backwards-compatibility for auto-configuration.
     */
    public void setIgnoreVirtual(boolean ignore) {
        setIgnoreUnmapped(ignore);
    }

    /**
     * Whether to use type alises for primary key values in place of class
     * names. Defaults to false.
     */
    public boolean getUseAliases() {
        return _aliases;
    }

    /**
     * Whether to use type alises for primary key values in place of class
     * names. Defaults to false.
     */
    public void setUseAliases(boolean aliases) {
        _aliases = aliases;
    }

    protected synchronized Status getStatus(ClassMapping mapping) {
        if (mapping == null)
            return null;
        String key = getKey(mapping, false);
        Status stat = (Status) _stats.get(key);
        if (stat == null) {
            stat = new Status();
            _stats.put(key, stat);
        }
        return stat;
    }

    protected Column addPrimaryKeyColumn(Table table) {
        DBDictionary dict = getConfiguration().getDBDictionaryInstance();
        Column pkColumn = table.addColumn(dict.getValidColumnName(
            getPrimaryKeyColumnIdentifier(), table));
        pkColumn.setType(dict.getPreferredType(Types.VARCHAR));
        pkColumn.setJavaType(JavaTypes.STRING);
        pkColumn.setSize(dict.characterColumnSize);
        return pkColumn;
    }

    protected Object getPrimaryKey(ClassMapping mapping) {
        if (mapping == null)
            return null;
        return getKey(mapping, true);
    }

    private String getKey(ClassMapping mapping, boolean db) {
        if (_ignore) {
            while (mapping.getMappedPCSuperclassMapping() != null)
                mapping = mapping.getMappedPCSuperclassMapping();
        } else {
            while (mapping.getPCSuperclass() != null)
                mapping = mapping.getPCSuperclassMapping();
        }
        if (_aliases)
            return mapping.getTypeAlias();
        return mapping.getDescribedType().getName();
    }

    /////////
    // Main
    /////////

    /**
     * Usage: java org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq [option]*
     * -action/-a &lt;add | drop | get | set&gt;
     * [class name | .java file | .class file | .jdo file] [value]
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The
     * path or resource name of a OpenJPA properties file containing
     * information such as connection data as
     * outlined in {@link JDBCConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link JDBCConfiguration} can be set by
     * using their	names and supplying a value.</li>
     * </ul>
     *  The various actions are as follows.
     * <ul>
     * <li><i>add</i>: Create the sequence table.</li>
     * <li><i>drop</i>: Drop the sequence table.</li>
     * <li><i>get</i>: Print the current sequence value for the given
     * class.</li>
     * <li><i>set</i>: Set the sequence value for the given class.</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws Exception {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                try {
                    return ClassTableJDBCSeq.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.out.println(_loc.get("clstable-seq-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if invalid options were given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts) throws Exception {
        String action = opts.removeProperty("action", "a", null);
        Configurations.populateConfiguration(conf, opts);
        return run(conf, args, action, null, null);
    }

    /**
     * Run the tool. Return false if an invalid option was given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        String action, MappingRepository repos, ClassLoader loader)
        throws Exception {
        ClassTableJDBCSeq seq = new ClassTableJDBCSeq();
        String props = Configurations.getProperties(conf.getSequence());
        Configurations.configureInstance(seq, conf, props);

        if (ACTION_DROP.equals(action)) {
            if (args.length != 0)
                return false;
            seq.dropTable();
        } else if (ACTION_ADD.equals(action)) {
            if (args.length != 0)
                return false;
            seq.refreshTable();
        } else if (ACTION_GET.equals(action) || ACTION_SET.equals(action)) {
            if (args.length == 0)
                return false;

            if (loader == null)
                loader = conf.getClassResolverInstance().getClassLoader(
                    ClassTableJDBCSeq.class, null);

            ClassArgParser cap = conf.getMetaDataRepositoryInstance()
                .getMetaDataFactory().newClassArgParser();
            cap.setClassLoader(loader);
            Class<?> cls = cap.parseTypes(args[0])[0];

            if (repos == null)
                repos = conf.getMappingRepositoryInstance();
            ClassMapping mapping = repos.getMapping(cls, null, true);

            Connection conn = conf.getDataSource2(null).getConnection();
            try {
                long cur = seq.getSequence(mapping, conn);
                if (ACTION_GET.equals(action)) {
                    // START - ALLOW PRINT STATEMENTS
                    System.out.println(mapping + ": " + cur);
                    // STOP - ALLOW PRINT STATEMENTS
                }else {
                    long set;
                    if (args.length > 1)
                        set = Long.parseLong(args[1]);
                    else
                        set = cur + seq.getAllocate();
                    if (set < cur)
                        set = cur;
                    else {
                        Status stat = seq.getStatus(mapping);
                        seq.setSequence(null, stat, (int) (set - cur), true,
                            conn);
                        set = stat.seq;
                    }
                    // START - ALLOW PRINT STATEMENTS
                    System.err.println(mapping + ": " + set);
                    // STOP - ALLOW PRINT STATEMENTS
                }
            } catch (NumberFormatException nfe) {
                return false;
            } finally {
                try {
                    conn.close();
                } catch (SQLException se) {
                }
            }
        } else
            return false;
        return true;
    }
}
