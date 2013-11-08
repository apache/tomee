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
 * Executes the schema generation via the OpenJPA MappingTool.
 * 
 * @version $Id$
 * @since 1.0
 * @goal schema
 * @phase process-classes
 * @requiresDependencyResolution compile
 * 
 */
public class OpenJpaSchemaMojo extends AbstractOpenJpaMappingToolMojo {

    /**
     * The action to take on the schema.
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
     *
     * @parameter default-value="add"
     */
    protected String schemaAction;
    /**
     * used for passing the schemaAction parameter to the mapping tool
     */
    protected static final String OPTION_SCHEMA_ACTION = "schemaAction";

    /**
     * Use this option to write the planned schema to an XML document
     * rather than modify the database. The document can then be manipulated and
     * committed to the database with the schema tool
     *
     * @parameter default-value="${project.build.directory}/schema.xml"
     */
    protected File schemaFile;
    /**
     * used for passing the schemaFile parameter to the mapping tool
     */
    protected static final String OPTION_SCHEMA_FILE = "schemaFile";

    /**
     * @return Options filled with all necessary plugin parameters
     */
    protected Options getOptions() throws MojoExecutionException {
        // options
        Options opts = createOptions();

        // put the standard options into the list also
        opts.put(OPTION_SCHEMA_ACTION, schemaAction);

        opts.put(OPTION_SCHEMA_FILE, schemaFile.getPath());

        if (action != null) {
            opts.put(OPTION_ACTION, action);
        }
        return opts;
    }
}
