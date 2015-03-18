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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.cli.SystemExitException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// this helps to know which setters/properties we can use for nor owned code (OracleXaDataSource is a perfect example)
public final class ListSetters {
    public static void main(final String[] args) throws Exception {
        final Options options = new Options();
        options.addOption(OptionBuilder
                .isRequired(true).hasArg(true)
                .withLongOpt("class").withDescription("the class to introspect").create("c"));

        final CommandLine line;
        try {
            line = new PosixParser().parse(options, args);
        } catch (final ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return;
        }

        final String clazz = line.getOptionValue("class");
        final Collection<Class<?>> ancestors = Classes.ancestors(Thread.currentThread().getContextClassLoader().loadClass(clazz));
        final List<String> list = new LinkedList<>();
        for (final Class<?> c : ancestors) {
            for (final Method m : c.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers())) {
                    continue;
                }

                if (!m.getName().startsWith("set")) {
                    continue;
                }

                if (m.getGenericParameterTypes().length != 1) {
                    continue;
                }

                list.add(m.getName().substring(3));
            }
        }
        Collections.sort(list);
        for (final String s : list) {
            System.out.println("- " + s);
        }
    }

    private static void help(final Options options) {
        new HelpFormatter().printHelp("setters [options] <value>", "\n", options, "\n");
    }

    private ListSetters() {
        // no-op
    }
}
