/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.client.util;

import jug.client.command.api.Command;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.UrlSet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager {

    private static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());
    private static final Map<String, Class<?>> COMMANDS = new TreeMap<String, Class<?>>();

    static {
        final ClassLoader loader = CommandManager.class.getClassLoader();
        try {
            UrlSet urlSet = new UrlSet(loader);
            urlSet = urlSet.exclude(loader);
            urlSet = urlSet.include(CommandManager.class.getProtectionDomain().getCodeSource().getLocation());

            final IAnnotationFinder finder = new AnnotationFinder(new ConfigurableClasspathArchive(loader, urlSet.getUrls()));
            for (Annotated<Class<?>> cmd : finder.findMetaAnnotatedClasses(Command.class)) {
                try {
                    final Command annotation = cmd.getAnnotation(Command.class);
                    final String key = annotation.name();
                    if (!COMMANDS.containsKey(key)) {
                        COMMANDS.put(key, cmd.get());
                    } else {
                        LOGGER.warning("command " + key + " already exists, this one will be ignored ( " + annotation.description() + ")");
                    }
                } catch (Exception e) {
                    // command ignored
                }
            }
        } catch (RuntimeException | IOException e) {
            LOGGER.log(Level.SEVERE, "an error occured while getting commands", e);
        }
    }

    public static Map<String, Class<?>> getCommands() {
        return COMMANDS;
    }

    public static int size() {
        return COMMANDS.size();
    }

    public static Set<String> keys() {
        return COMMANDS.keySet();
    }
}
