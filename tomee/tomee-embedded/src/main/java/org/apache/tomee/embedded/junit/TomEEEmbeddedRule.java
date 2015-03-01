/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded.junit;

import org.apache.openejb.util.NetworkUtil;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;

public class TomEEEmbeddedRule implements TestRule {
    private final Configuration configuration;
    private final File docBase;
    private boolean resetSystemProperties = true;
    private final String context;
    private final Collection<Object> injects = new LinkedList<>();

    public TomEEEmbeddedRule() {
        this(new Configuration().http(NetworkUtil.getNextAvailablePort()).dir(autoDir()), "");
    }

    public TomEEEmbeddedRule(final Configuration configuration, final String context) {
        this.configuration = configuration;
        this.context = context;

        final File mvnDocBase = new File("src/main/webapp");
        if (mvnDocBase.isDirectory()) {
            docBase = mvnDocBase;
        } else {
            docBase = null;
        }
    }

    public TomEEEmbeddedRule(final Configuration configuration, final String context, final File docBase) {
        this.configuration = configuration;
        this.docBase = docBase;
        this.context = context;
    }

    public TomEEEmbeddedRule injectOn(final Object instance) {
        this.injects.add(instance);
        return this;
    }

    public TomEEEmbeddedRule resetSystemPropertiesAfter(final boolean value) {
        resetSystemProperties = value;
        return this;
    }

    public TomEEEmbeddedRule stopInjectionOn(final Object instance) {
        this.injects.remove(instance);
        return this;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public int getPort() {
        return configuration.getHttpPort();
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        if (configuration.getDir() == null) {
            configuration.dir(autoDir());
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Properties properties = new Properties();
                if (resetSystemProperties) {
                    properties.putAll(System.getProperties());
                }

                try (final Container container = new Container(configuration)
                                     .deployClasspathAsWebApp(context, docBase, toCallers())) {
                    for (final Object o : injects) {
                        container.inject(o);
                    }
                    statement.evaluate();
                } finally {
                    if (resetSystemProperties) { // issue is we set System Properties like default loader which breaks other tests
                        System.getProperties().clear();
                        System.getProperties().putAll(properties);
                    }
                }
            }
        };
    }

    private List<String> toCallers() {
        if (injects.isEmpty()) {
            return null;
        }
        final Collection<String> callers = new HashSet<>();
        for (final Object o : injects) {
            callers.add(o.getClass().getName());
        }
        return new LinkedList<>(callers);
    }

    private static String autoDir() {
        for (final String test : asList("target", "../target", "workdir")) {
            final File dir = new File(test);
            if (dir.isDirectory()) {
                return new File(dir, "tomee-embedded_" + System.identityHashCode(test)).getAbsolutePath();
            }
        }
        return null;
    }
}
