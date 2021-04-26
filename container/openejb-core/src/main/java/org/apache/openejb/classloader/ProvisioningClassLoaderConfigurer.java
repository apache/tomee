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

package org.apache.openejb.classloader;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration
 * <configurer prefix>.configuration = /foo/bar/config.txt
 *
 * Handled file format:
 * -xbean
 * +http://..../camel-core.jar
 * +mvn:org.foo:bar:1.0
 *
 * The maven like urls needs the openejb-provisinning module
 *
 * Note: if a line doesn't start with '+' it is considered as an addition
 */
public class ProvisioningClassLoaderConfigurer implements ClassLoaderConfigurer {
    // just some default if one is not set
    private URL[] added = new URL[0];
    private Filter excluded = FalseFilter.INSTANCE;

    @Override
    public URL[] additionalURLs() {
        return added;
    }

    @Override
    public boolean accept(final URL url) {
        try {
            final File file = URLs.toFile(url);
            return !excluded.accept(file.getName());
        } catch (final IllegalArgumentException iae) {
            return true;
        }
    }

    public void setConfiguration(final String configFile) {
        final Collection<URL> toAdd = new ArrayList<>();
        final Collection<String> toExclude = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));

            String line;
            while ((line = reader.readLine()) != null) {
                line = PropertyPlaceHolderHelper.replace(line.trim());
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("-")) {
                    toExclude.add(line);
                } else {
                    if (line.startsWith("+")) {
                        line = line.substring(1);
                    }

                    String location = line;
                    String algo = "MD5";
                    String hash = null;
                    final boolean validJar = line.contains("|");

                    if (validJar) {
                        final String[] segments = line.split("|");
                        location = segments[0];
                        if (segments.length >= 2) {
                            hash = segments[1];
                        }
                        if (segments.length >= 3) {
                            algo = segments[2].trim();
                        }

                    }

                    final Set<URL> repos = toUrls(ProvisioningUtil.realLocation(location));
                    toAdd.addAll(repos);

                    if (validJar) {
                        final String computedHash = Files.hash(repos, algo);
                        if (!computedHash.equals(hash)) {
                            throw new IllegalStateException("Hash of " + location + "(" + computedHash + ") doesn't match expected one (" + hash + ")");
                        }
                    }
                }
            }

        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ProvisioningClassLoaderConfigurer.class).error("Can't read " + configFile, e);
        } finally {
            IO.close(reader);
        }

        added = toAdd.toArray(new URL[toAdd.size()]);
        if (toExclude.size() > 0) {
            excluded = Filters.prefixes(toExclude.toArray(new String[toExclude.size()]));
        }
    }

    private static Set<URL> toUrls(final Set<String> strings) {
        final Set<URL> urls = new HashSet<>();
        for (final String s : strings) {
            try {
                urls.add(new File(s).toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return urls;
    }
}
