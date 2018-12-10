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
package org.apache.openejb.loader;

import org.apache.openejb.loader.provisining.ProvisioningResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

// Note: maybe you want to use org.apache.openejb.loader.provisining.ProvisioningResolver now
// instead of this class
public final class ProvisioningUtil {
    private static final String ADDITIONAL_LIB_CONFIG = "provisioning.properties";
    private static final String ZIP_KEY = "zip";
    private static final String DESTINATION_KEY = "destination";
    private static final String JAR_KEY = "jar";

    // lazy since shouldn't be useful in a real tomee/openejb
    private static volatile ProvisioningResolver DEFAULT_PROVISIONING_RESOLVER = null;

    private ProvisioningUtil() {
        // no-op
    }

    public static Collection<File> addAdditionalLibraries() throws IOException {
        final File conf = SystemInstance.get().getConf(ADDITIONAL_LIB_CONFIG);
        if (conf == null || !conf.exists()) {
            return Collections.emptyList();
        }

        final Properties additionalLibProperties = IO.readProperties(conf);

        final ProvisioningResolver resolver = SystemInstance.get().getComponent(ProvisioningResolver.class);
        if (resolver == null) {
            throw new IllegalStateException("SystemInstance not ready");
        }

        final List<String> libToCopy = new LinkedList<>();
        final String toCopy = additionalLibProperties.getProperty(JAR_KEY);
        if (toCopy != null) {
            for (final String lib : toCopy.split(",")) {
                libToCopy.addAll(resolver.realLocation(lib.trim()));
            }
        }
        final String toExtract = additionalLibProperties.getProperty(ZIP_KEY);
        if (toExtract != null) {
            for (final String zip : toExtract.split(",")) {
                final Set<String> strings = resolver.realLocation(zip);
                if (strings.size() != 1) {
                    throw new IllegalArgumentException("Didnt find a single zip: " + strings);
                }
                libToCopy.addAll(extract(strings.iterator().next()));
            }
        }

        final File destination;
        if (additionalLibProperties.containsKey(DESTINATION_KEY)) {
            destination = new File(additionalLibProperties.getProperty(DESTINATION_KEY));
        } else {
            destination = new File(SystemInstance.get().getBase().getDirectory(), Embedder.ADDITIONAL_LIB_FOLDER);
        }
        if (!destination.exists()) {
            Files.mkdirs(destination);
        }

        final Collection<File> newFiles = new ArrayList<>(libToCopy.size());
        for (final String lib : libToCopy) {
            newFiles.add(copy(new File(lib), destination));
        }
        return newFiles;
    }

    private static File copy(final File file, final File lib) throws IOException {
        final File dest = new File(lib, file.getName());
        if (dest.exists()) {
            return dest;
        }
        IO.copy(file, dest);
        return dest;
    }

    private static Collection<String> extract(final String zip) throws IOException {
        final File tmp = new File(SystemInstance.get().getBase().getDirectory(), ProvisioningResolver.TEMP_DIR);
        if (!tmp.exists()) {
            try {
                Files.mkdirs(tmp);
            } catch (final Files.FileRuntimeException fre) {
                // ignored
            }
        }

        final File zipFile = new File(zip);
        final File extracted = new File(tmp, zipFile.getName().replace(".zip", ""));
        if (extracted.exists()) {
            return list(extracted);
        } else {
            Files.mkdirs(extracted);
        }

        Zips.unzip(zipFile, extracted);
        return list(extracted);
    }

    private static Collection<String> list(final File dir) {
        if (dir == null) {
            return Collections.emptyList();
        }

        final Collection<String> libs = new ArrayList<>();
        final File[] files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        for (final File file : files) {
            if (file.isDirectory()) {
                libs.addAll(list(file));
            } else {
                libs.add(file.getAbsolutePath());
            }
        }
        return libs;
    }

    public static Set<String> realLocation(final String location) {
        final boolean initialized = SystemInstance.isInitialized();
        if (!initialized) {
            if (DEFAULT_PROVISIONING_RESOLVER == null) {
                synchronized (ProvisioningUtil.class) {
                    if (DEFAULT_PROVISIONING_RESOLVER == null) {
                        DEFAULT_PROVISIONING_RESOLVER = new ProvisioningResolver();
                    }
                }
            }
            return DEFAULT_PROVISIONING_RESOLVER.realLocation(location);
        }
        return SystemInstance.get().getComponent(ProvisioningResolver.class).realLocation(location);
    }
}
