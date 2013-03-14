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
package org.apache.openejb.config;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public final class QuickJarsTxtParser {
    public static final String FILE_NAME = "jars.txt";

    private QuickJarsTxtParser() {
         // no-op
    }

    public static Collection<URL> parse(final File file) {
        if (!file.exists()) {
            return new ArrayList<URL>(); // need to be modifiable
        }

        final Collection<URL> deps = new ArrayList<URL>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                final String value = line.trim();
                if (line.startsWith("#") || value.isEmpty()) {
                    continue;
                }

                deps.addAll(Files.listJars(new File(ProvisioningUtil.realLocation(value)).getAbsolutePath()));
            }
        } catch (final Throwable e) {
            Logger.getInstance(LogCategory.OPENEJB, QuickContextXmlParser.class.getName())
                    .warning("QuickJarsTxtParser#parse: Failed to read provided stream");
        } finally {
            IO.close(reader);
        }

        return deps;
    }
}
