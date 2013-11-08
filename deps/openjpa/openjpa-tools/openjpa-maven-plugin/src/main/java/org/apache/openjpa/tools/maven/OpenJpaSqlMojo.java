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
package org.apache.openjpa.tools.maven;


import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.openjpa.lib.util.Options;

/**
 * Executes the SQL generation via the OpenJPA MappingTool.
 * 
 * @version $Id$
 * @since 1.0
 * @goal sql
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class OpenJpaSqlMojo extends AbstractOpenJpaMappingToolMojo {

    /**
     * The action to take for generating the SQL.
     * Actions can be composed in a comma-separated list of one of the following items:
     * <ul>
     * <li>add (see MappingTool#ACTION_ADD)</li>
     * <li>refresh (see MappingTool#ACTION_REFRESH)</li>
     * <li>drop (see MappingTool#ACTION_DROP)</li>
     * <li>buildSchema (see MappingTool#ACTION_BUILD_SCHEMA)</li>
     * <li>import (see MappingTool#ACTION_IMPORT)</li>
     * <li>export (see MappingTool#ACTION_EXPORT)</li>
     * <li>validate (see MappingTool#ACTION_VALIDATE)</li>
     * </ul>
     * Technically this is the same like the {@code schemaAction}, but we have to
     * split it for the plugin to allow different actions for generating the mapping
     * and generating the SQL files.
     *
     * @parameter default-value="build"
     */
    protected String sqlAction;
    /**
     * internally the options is named 'schemaAction'!
     */
    protected static final String OPTION_SQL_ACTION = "schemaAction";

    /**
     * Use this option to write the planned schema modifications to a SQL
     * script. Combine this with a schemaAction
     * of "build" to generate a script that recreates the schema for the
     * current mappings, even if the schema already exists.
     *
     * @parameter default-value="${project.build.directory}/database.sql"
     */
    protected File sqlFile;
    /**
     * used for passing the sqlFile parameter to the mapping tool
     */
    protected static final String OPTION_SQL_FILE = "sqlFile";


    /**
     * Use this option to write the planned schema modifications to
     * the database. If this is set, the sqlFile setting (if any) will
     * be ignored.
     *
     * @parameter default-value="false"
     */
    protected boolean modifyDatabase;


    /**
     * @return Options filled with all necessary plugin parameters
     */
    protected Options getOptions() throws MojoExecutionException {
        // options
        Options opts = createOptions();


        if (!modifyDatabase) {

            opts.put(OPTION_SQL_FILE, sqlFile.getPath());
        } else {
            if (sqlAction.equals("build")) {
                // build is not valid if we write to the database directly
                sqlAction = "refresh";
            }
        }

        // put the standard options into the list also
        opts.put(OPTION_SQL_ACTION, sqlAction);

        return opts;
    }
}
