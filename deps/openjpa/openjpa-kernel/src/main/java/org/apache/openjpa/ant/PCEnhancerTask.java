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
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.lib.ant.AbstractTask;
import org.apache.openjpa.lib.conf.ConfigurationImpl;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.meta.MetaDataRepository;

/**
 * Executes the enhancer on the specified files. This task can take
 * the following arguments:
 * <ul>
 * <li><code>directory</code></li>
 * <li><code>addDefaultConstructor</code></li>
 * <li><code>tmpClassLoader</code></li>
 * <li><code>enforcePropertyRestrictions</code></li>
 * </ul>
 */
public class PCEnhancerTask
    extends AbstractTask {

    protected PCEnhancer.Flags flags = new PCEnhancer.Flags();
    protected String dirName = null;

    /**
     * Set the output directory we want the enhancer to write to.
     */
    public void setDirectory(String dirName) {
        this.dirName = dirName;
    }

    /**
     * Set whether or not the enhancer should add a no-args constructor
     * to any PC that does not have a no-args constructor.
     */
    public void setAddDefaultConstructor(boolean addDefCons) {
        flags.addDefaultConstructor = addDefCons;
    }

    /**
     * Set whether to fail if the persistent type uses property access and
     * bytecode analysis shows that it may be violating OpenJPA's property
     * access restrictions.
     */
    public void setEnforcePropertyRestrictions(boolean fail) {
        flags.enforcePropertyRestrictions = fail;
    }

    /**
     * Set whether or not to use a default class loader for loading
     * the unenhanced classes.
     */
    public void setTmpClassLoader(boolean tmpClassLoader) {
        flags.tmpClassLoader = tmpClassLoader;
    }

    protected ConfigurationImpl newConfiguration() {
        return new OpenJPAConfigurationImpl();
    }

    protected void executeOn(String[] files)
        throws IOException {
        flags.directory = (dirName == null) ? null
            : Files.getFile(dirName, getClassLoader());
        OpenJPAConfiguration conf = (OpenJPAConfiguration) getConfiguration();
        MetaDataRepository repos = conf.newMetaDataRepositoryInstance();
        PCEnhancer.run(conf, files, flags, repos, null, getClassLoader ());
	}
}
