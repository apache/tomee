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
package org.apache.openjpa.ant;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.ant.AbstractTask;
import org.apache.openjpa.lib.conf.ConfigurationImpl;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.MetaDataTool;

/**
 * Executes the metadata tool on the specified files. This task can
 * take the following arguments:
 * <ul>
 * <li><code>file</code></li>
 * </ul>
 */
public class MetaDataToolTask
    extends AbstractTask {

    private static final Localizer _loc = Localizer.forPackage
        (MetaDataToolTask.class);

    protected MetaDataTool.Flags flags = new MetaDataTool.Flags();
    protected String fileName = null;

    /**
     * Set the tool action.
     */
    public void setAction(Action act) {
        flags.action = act.getValue();
    }

    /**
     * Set the file to write the metadata to.
     */
    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    protected ConfigurationImpl newConfiguration() {
        return new OpenJPAConfigurationImpl();
    }

    protected void executeOn(String[] files)
        throws IOException {
        ClassLoader loader = getClassLoader();
        if ("stdout".equals(fileName))
            flags.writer = new PrintWriter(System.out);
        else if ("stderr".equals(fileName))
            flags.writer = new PrintWriter(System.err);
        else if (fileName != null)
            flags.file = Files.getFile(fileName, loader);
        if (!MetaDataTool.run((OpenJPAConfiguration) getConfiguration(), files,
            flags, null, loader))
            throw new BuildException(_loc.get("bad-conf", "MetaDataToolTask")
                .getMessage());
    }

    public static class Action
        extends EnumeratedAttribute {

        public String[] getValues() {
            return MetaDataTool.ACTIONS;
        }
	}
}
