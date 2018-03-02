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
package org.apache.tomee.microprofile;

import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.tomee.installer.Paths;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

public class MicroProfileClassLoaderEnricher implements WebAppEnricher {
    private static final String[] MICROPROFILE_LIBS_IMPLS_PREFIXES = new String[]{
        "geronimo-config-impl"
    };

    @SuppressWarnings("Duplicates")
    @Override
    public URL[] enrichment(final ClassLoader webappClassLaoder) {
        final Collection<URL> urls = new HashSet<>();

        // from prefix
        final Paths paths = new Paths(new File(System.getProperty("openejb.home"))); // parameter is useless
        for (final String prefix : MICROPROFILE_LIBS_IMPLS_PREFIXES) {
            final File file = paths.findTomEELibJar(prefix);
            if (file != null) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    // ignored
                }
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }
}
