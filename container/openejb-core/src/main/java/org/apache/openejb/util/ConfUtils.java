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

package org.apache.openejb.util;

import org.apache.openejb.EnvProps;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @version $Rev$ $Date$
 */
public class ConfUtils {

    public static URL getConfResource(final String name) {
        URL resource = getResource(name);

        if (!EnvProps.extractConfigurationFiles()) {
            return resource;
        }

        try {

            final File loginConfig = ConfUtils.install(resource, name);

            if (loginConfig != null) {
                resource = loginConfig.toURI().toURL();
            }
        } catch (final IOException e) {
            // no-op
        }

        return resource;
    }

    public static URL getResource(final String name) {
        Enumeration<URL> resources = null;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources(name);
        } catch (final IOException e) {
            // DMB: Not sure why this version of getResource doesn't require checking
            // for IOException, but no matter.  Perhpas it may succeed where the other fails.
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }

        final URL resource = select(resources);
        return resource;
    }

    private static URL select(final Enumeration<URL> enumeration) {
        if (enumeration == null) {
            return null;
        }
        final ArrayList<URL> urls = Collections.list(enumeration);
        if (urls.size() == 0) {
            return null;
        }
        if (urls.size() == 1) {
            return urls.get(0);
        }

        // Sort so that the URL closest to openejb.base is first
        urls.sort(new UrlComparator(SystemInstance.get().getBase().getDirectory()));

        return urls.get(0);
    }

    public static File install(final String source, final String name) throws IOException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL resource = cl.getResource(source);
        return install(resource, name, false);
    }

    public static File install(final URL resource, final String name) throws IOException {
        return install(resource, name, false);
    }

    public static File install(final URL resource, final String name, final boolean overwrite) throws IOException {
        if (resource == null) {
            return null;
        }
        final SystemInstance system = SystemInstance.get();
        final File conf = system.getConf(null);
        if (conf != null && !conf.exists()) {
            return null;
        }
        final File file = new File(conf, name);
        if (file.exists() && !overwrite) {
            return file;
        }
        IO.copy(IO.read(resource), file);
        return file;
    }

}
