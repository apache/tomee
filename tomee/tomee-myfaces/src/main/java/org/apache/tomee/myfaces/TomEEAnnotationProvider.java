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
package org.apache.tomee.myfaces;

import org.apache.myfaces.config.annotation.DefaultAnnotationProvider;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.faces.context.ExternalContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TomEEAnnotationProvider extends DefaultAnnotationProvider {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEAnnotationProvider.class);

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(final ExternalContext ctx) {
        final ClassLoader cl = getClassLoader();

        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        if (builder == null) {
            throw new IllegalStateException("WebAppBuilder not found in SystemInstance. "
                    + "Ensure the following entry exists in the Tomcat server.xml file: <Listener className=\"org.apache.tomee.catalina.ServerListener\"/>"
            );
        }

        final Map<Class<? extends Annotation>, Set<Class<?>>> map = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();

        final Map<ClassLoader, Map<String, Set<String>>> jsfClasses = builder.getJsfClasses();
        if (jsfClasses == null) {
            throw new IllegalStateException("JsfClasses not found in WebAppBuilder");
        }

        final Map<String, Set<String>> scanned = jsfClasses.get(cl);
        if (scanned == null) {
            return Collections.emptyMap();
        }

        for (final Map.Entry<String, Set<String>> entry : scanned.entrySet()) {
            final Class<? extends Annotation> annotation;
            try {
                annotation = (Class<? extends Annotation>) cl.loadClass(entry.getKey());
            } catch (final ClassNotFoundException e) {
                continue;
            }

            final Set<String> list = entry.getValue();
            final Set<Class<?>> annotated = new HashSet<Class<?>>(list.size());
            for (final String name : list) {
                try {
                    annotated.add(cl.loadClass(name));
                } catch (final ClassNotFoundException ignored) {
                    LOGGER.warning("class '" + name + "' was found but can't be loaded as a JSF class");
                }
            }

            map.put(annotation, annotated);
        }
        return map;
    }

    private ClassLoader getClassLoader() {
        final ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null) {
            return getClass().getClassLoader();
        }
        return loader;
    }
}
