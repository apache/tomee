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
package org.apache.tomee.catalina;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public final class TomEEClassLoaderHelper {
    public static final String TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_SKIP = "tomee.webapp.classloader.enrichment.skip";
    public static final String TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT = "tomee.webapp.classloader.enrichment";

    private static final String[] DEFAULT_JAR_TO_ADD_CLASS_HELPERS = new String[] {
            // openejb-jsf and openwebbeans-jsf to be able to embedded the jsf impl keeping CDI features
            "org.apache.openejb.jsf.CustomApplicationFactory",
            "org.apache.webbeans.jsf.OwbApplicationFactory",

            // JPA integration: mainly JTA integration
            "org.apache.openejb.jpa.integration.MakeTxLookup"
    };
    private static final String[] JAR_TO_ADD_CLASS_HELPERS;

    static {
        final Collection<String> classes = new ArrayList<String>();
        if (!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_SKIP, false)) {
            classes.addAll(Arrays.asList(DEFAULT_JAR_TO_ADD_CLASS_HELPERS));

            final String additionalEnrichments = SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT, "");
            if (additionalEnrichments != null && !additionalEnrichments.isEmpty()) {
                for (String name : additionalEnrichments.split(",")) {
                    classes.add(name.trim());
                }
            }
        }
        JAR_TO_ADD_CLASS_HELPERS = classes.toArray(new String[classes.size()]);
    }

    private TomEEClassLoaderHelper() {
        // no-op
    }

    public static URL[] tomEEWebappIntegrationLibraries() {
        final ClassLoader cl = TomEEClassLoaderHelper.class.getClassLoader(); // reference classloader = standardclassloader
        final Collection<URL> urls = new ArrayList<URL>();
        for (String name : JAR_TO_ADD_CLASS_HELPERS) {
            try {
                final Class<?> clazz = cl.loadClass(name);
                if (!clazz.getClassLoader().equals(OpenEJB.class.getClassLoader())) { // already provided?
                    continue;
                }

                final URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
                if (url == null) {
                    continue;
                }

                urls.add(url);
            } catch (Exception e) {
                // ignore
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }
}
