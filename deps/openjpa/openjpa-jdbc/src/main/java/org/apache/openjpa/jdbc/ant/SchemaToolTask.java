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
package org.apache.openjpa.jdbc.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.schema.SchemaTool;
import org.apache.openjpa.lib.ant.AbstractTask;
import org.apache.openjpa.lib.conf.ConfigurationImpl;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Executes the {@link SchemaTool} on the specified XML schema definition
 * files. This task can take the following arguments:
 * <ul>
 * <li><code>action</code></li>
 * <li><code>ignoreErrors</code></li>
 * <li><code>dropTables</code></li>
 * <li><code>dropSequences</code></li>
 * <li><code>openjpaTables</code></li>
 * <li><code>primaryKeys</code></li>
 * <li><code>foreignKeys</code></li>
 * <li><code>indexes</code></li>
 * <li><code>sequences</code></li>
 * <li><code>record</code></li>
 * <li><code>file</code></li>
 * </ul> Of these arguments, only <code>action</code> is required.
 */
public class SchemaToolTask
    extends AbstractTask {

    private static final Localizer _loc = Localizer.forPackage
        (SchemaToolTask.class);

    protected SchemaTool.Flags flags = new SchemaTool.Flags();
    protected String file = null;

    /**
     * Set the enumerated SchemaTool action type.
     */
    public void setAction(Action act) {
        flags.action = act.getValue();
    }

    /**
     * Set whether we want the SchemaTool to ignore SQL errors.
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        flags.ignoreErrors = ignoreErrors;
    }

    /**
     * Set whether the SchemaTool should drop tables.
     */
    public void setDropTables(boolean dropTables) {
        flags.dropTables = dropTables;
    }

    /**
     * Set whether to drop or reflect on OpenJPA tables.
     */
    public void setOpenJPATables(boolean openjpaTables) {
        flags.openjpaTables = openjpaTables;
    }

    /**
     * Set whether the SchemaTool should drop sequences.
     */
    public void setDropSequences(boolean dropSequences) {
        flags.dropSequences = dropSequences;
    }

    /**
     * Set whether the SchemaTool should manipulate sequences.
     */
    public void setSequences(boolean sequences) {
        flags.sequences = sequences;
    }

    /**
     * Set whether to generate primary key information.
     */
    public void setPrimaryKeys(boolean pks) {
        flags.primaryKeys = pks;
    }

    /**
     * Set whether to generate foreign key information.
     */
    public void setForeignKeys(boolean fks) {
        flags.foreignKeys = fks;
    }

    /**
     * Set whether to generate index information.
     */
    public void setIndexes(boolean idxs) {
        flags.indexes = idxs;
    }

    /**
     * Set whether the SchemaTool should record to the schema factory.
     */
    public void setRecord(boolean record) {
        flags.record = record;
    }

    /**
     * Set the output file we want the SchemaTool to write to.
     */
    public void setFile(String file) {
        this.file = file;
    }

    protected ConfigurationImpl newConfiguration() {
        return new JDBCConfigurationImpl();
    }

    protected void executeOn(String[] files)
        throws Exception {
        if (SchemaTool.ACTION_IMPORT.equals(flags.action))
            assertFiles(files);

        ClassLoader loader = getClassLoader();
        flags.writer = Files.getWriter(file, loader);
        if (!SchemaTool.run((JDBCConfiguration) getConfiguration(), files,
            flags, loader))
            throw new BuildException(_loc.get("bad-conf", "SchemaToolTask")
                .getMessage());
    }

    public static class Action
        extends EnumeratedAttribute {

        public String[] getValues() {
            return SchemaTool.ACTIONS;
        }
    }
}

