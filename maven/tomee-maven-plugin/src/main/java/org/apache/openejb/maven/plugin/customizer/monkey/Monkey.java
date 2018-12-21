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
package org.apache.openejb.maven.plugin.customizer.monkey;

import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.customizer.monkey.file.PatchFolderFinder;
import org.apache.openejb.maven.plugin.customizer.monkey.index.FileIndexer;
import org.apache.openejb.maven.plugin.customizer.monkey.index.Item;
import org.apache.openejb.maven.plugin.customizer.monkey.jar.JarPatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A customizer (see tomee-maven-plugin) allowing to take current project binaries (target/classes)
 * and merge them in tomee for patch cases.
 * <p>
 * Note: for now it needs to overlap with [tomee]/lib/*.jar.
 * <p>
 * Jar will get patched and renamed with tomee-monkey-[date] suffix.
 */
public class Monkey implements Runnable {
    /**
     * The constant MONKEY_CONFIGURATION_FILE.
     */
    public static final String MONKEY_CONFIGURATION_FILE = "tomee-monkey.properties";
    private final File base;
    private final File classes;
    private final File tempFolder;
    private final Properties configuration;

    /**
     * Instantiates a new Monkey.
     *
     * @param base the base
     */
    public Monkey(final File base) {
        this.base = base;
        final File target = new PatchFolderFinder().findTarget(base);

        this.classes = new File(target, "classes");
        if (!this.classes.isDirectory()) {
            throw new IllegalArgumentException("target/classes doesn't exist");
        }

        this.tempFolder = new File(target, "MonkeyPatcher_" + System.currentTimeMillis());
        this.tempFolder.mkdirs();

        final File configurationFile = new File(classes, MONKEY_CONFIGURATION_FILE);
        configuration = new Properties();
        if (configurationFile.isFile()) {
            InputStream is = null;
            try {
                is = new FileInputStream(configurationFile);
                configuration.load(is);
            } catch (final FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            } finally {
                IO.close(is);
            }
        }
    }

    @Override
    public void run() {
        final PrintStream stream = System.out;
        final FileIndexer indexer = new FileIndexer(base, classes, configuration, MONKEY_CONFIGURATION_FILE).index().dump(stream);
        stream.println();
        final JarPatcher patcher = new JarPatcher();

        stream.println("Patcher:");
        for (final Map.Entry<File, List<Item>> entry : indexer.getIndex().entrySet()) {
            patcher.patch(stream, tempFolder, entry.getKey(), entry.getValue());
        }
        stream.println();

        stream.println(indexer.getIndex().size() + " jar patched.");
    }
}
