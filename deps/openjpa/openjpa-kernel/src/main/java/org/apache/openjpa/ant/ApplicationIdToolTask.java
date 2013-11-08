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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.enhance.ApplicationIdTool;
import org.apache.openjpa.lib.ant.AbstractTask;
import org.apache.openjpa.lib.conf.ConfigurationImpl;
import org.apache.openjpa.lib.util.CodeFormat;
import org.apache.openjpa.lib.util.Files;

/**
 * <p>Executes the application id tool on the specified files.  This task
 * can take the following arguments:
 * <ul>
 * <li><code>directory</code></li>
 * <li><code>ignoreErrors</code></li>
 * <li><code>name</code></li>
 * <li><code>suffix</code></li>
 * <li><code>token</code></li>
 * </ul>
 * It can also take an embedded <code>codeFormat</code> element with attributes
 * for the bean properties of the {@link CodeFormat}.</p>
 */
public class ApplicationIdToolTask
    extends AbstractTask {

    protected ApplicationIdTool.Flags flags = new ApplicationIdTool.Flags();
    protected String dirName = null;

    /**
     * Default constructor.
     */
    public ApplicationIdToolTask() {
        flags.format = new CodeFormat();
    }

    /**
     * Set the output directory we want the enhancer to write to.
     */
    public void setDirectory(String dirName) {
        this.dirName = dirName;
    }

    /**
     * Set whether to ignore errors.
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        flags.ignoreErrors = ignoreErrors;
    }

    /**
     * Set the name of the identity class; with this option you must supply
     * exactly one class to run on.
     */
    public void setName(String name) {
        flags.name = name;
    }

    /**
     * Set a suffix to append to persistent classes to form their identity
     * class name.
     */
    public void setSuffix(String suffix) {
        flags.suffix = suffix;
    }

    /**
     * Set the token to separate stringified primary key field values.
     */
    public void setToken(String token) {
        flags.token = token;
    }

    /**
     * Create the embedded code format element.
     */
    public Object createCodeFormat() {
        return flags.format;
    }

    protected ConfigurationImpl newConfiguration() {
        return new OpenJPAConfigurationImpl();
    }

    protected void executeOn(String[] files)
        throws IOException, ClassNotFoundException {
        flags.directory = (dirName == null) ? null
            : Files.getFile(dirName, getClassLoader());
        ApplicationIdTool.run((OpenJPAConfiguration) getConfiguration(), files,
            flags, getClassLoader ());
	}
}
