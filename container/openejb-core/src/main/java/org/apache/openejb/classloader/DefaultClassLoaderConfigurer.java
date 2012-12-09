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

import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultClassLoaderConfigurer implements ClassLoaderConfigurer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DefaultClassLoaderConfigurer.class);

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
        } catch (IllegalArgumentException iae) {
            return true;
        }
    }

    public void setAddedFolder(final String addedFolder) {
        final Collection<String> addedList = new ArrayList<String>();
        if (addedFolder != null) {
            final File parent = new File(addedFolder);
            if (parent.exists()) {
                final File[] files = parent.listFiles();
                if (files != null) {
                    for (final File f : files) {
                        final String name = f.getName();
                        if (f.isDirectory() || name.endsWith(".zip") || name.endsWith(".jar")) {
                            addedList.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        added = new URL[addedList.size()];
        int i = 0;
        for (final String path : addedList) {
            try {
                added[i++] = new File(path).toURI().toURL();
            } catch (MalformedURLException e) {
                LOGGER.warning("Can't add file " + path, e);
            }
        }
    }

    public void setExcludedListFile(final String excludedListFile) {
        String[] excludedPrefixes = null;
        if (excludedListFile != null) {
            final File excludedFile = new File(excludedListFile);
            if (excludedFile.exists()) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(excludedFile);
                    excludedPrefixes = NewLoaderLogic.readInputStreamList(is);
                } catch (FileNotFoundException e) {
                    LOGGER.error("can't read " + excludedListFile);
                } finally {
                    IO.close(is);
                }
            }
        }

        if (excludedPrefixes == null || excludedPrefixes.length == 0) {
            excluded = TrueFilter.INSTANCE;
        } else {
            excluded = Filters.prefixes(excludedPrefixes);
        }
    }

    private static class TrueFilter implements Filter {
        public static final TrueFilter INSTANCE = new TrueFilter();

        @Override
        public boolean accept(final String name) {
            return true;
        }
    }

    private static class FalseFilter implements Filter {
        public static final FalseFilter INSTANCE = new FalseFilter();

        @Override
        public boolean accept(final String name) {
            return true;
        }
    }
}
