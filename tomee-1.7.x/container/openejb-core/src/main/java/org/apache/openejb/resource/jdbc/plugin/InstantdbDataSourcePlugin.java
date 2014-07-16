/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.resource.jdbc.plugin;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.IOException;

public class InstantdbDataSourcePlugin extends AbstractDataSourcePlugin {

    @Override
    public String updatedUrl(final String jdbcUrl) {
        // jdbc:idb:conf/instantdb.properties
        final String prefix = "jdbc:idb:";
        final int index = jdbcUrl.indexOf(prefix);
        if (index == -1) {
            return jdbcUrl;
        }

        final String confFile = jdbcUrl.substring(index + prefix.length());

        final File base = SystemInstance.get().getBase().getDirectory();
        final File file = new File(base, confFile);


        if (file.exists()) {
            // The instantdb properties file is there, we're good
            return jdbcUrl;
        }

        if (!file.getParentFile().exists()) {
            // The directory the instantdb properties file should live in
            // doesn't exist, don't bother
            return jdbcUrl;
        }

        try {
            final ResourceFinder finder = new ResourceFinder("");
            final String defaultProperties = finder.findString("default.instantdb.properties");
            IO.copy(defaultProperties.getBytes(), file);
        } catch (final IOException e) {
            // TODO; Handle this
            e.printStackTrace();
        }

        return jdbcUrl;
    }

    public boolean enableUserDirHack() {
        return isActive(this);
    }

}
