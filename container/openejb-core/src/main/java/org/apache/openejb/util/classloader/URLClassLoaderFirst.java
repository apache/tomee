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
package org.apache.openejb.util.classloader;

import org.apache.openejb.loader.SystemInstance;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

// TODO: look SM usage, find a better name
public class URLClassLoaderFirst extends URLClassLoader {
    // log4j is optional, moreover it will likely not work if not skipped and loaded by a temp classloader
    private static final boolean SKIP_LOG4J = "true".equals(SystemInstance.get().getProperty("openejb.skip.log4j", "true")) && skipLib("org.apache.log4j.Logger");
    private static final boolean SKIP_MYFACES = "true".equals(SystemInstance.get().getProperty("openejb.skip.myfaces", "true")) && skipLib("org.apache.myfaces.spi.FactoryFinderProvider");
    // commons-net is only in tomee-plus
    private static final boolean SKIP_COMMONS_NET = skipLib("org.apache.commons.net.pop3.POP3Client");

    // - will not match anything, that's the desired default behavior
    private static final Collection<String> FORCED_SKIP = new ArrayList<String>();
    private static final Collection<String> FORCED_LOAD = new ArrayList<String>();

    static {
        reloadConfig();
    }

    public static void reloadConfig() {
        list(FORCED_SKIP, "openejb.classloader.forced-skip");
        list(FORCED_LOAD, "openejb.classloader.forced-load");
    }

    private static void list(final Collection<String> list, final String key) {
        list.clear();

        final String s = SystemInstance.get().getOptions().get(key, (String) null);
        if (s != null && !s.trim().isEmpty()) {
            list.addAll(Arrays.asList(s.trim().split(",")));
        }
    }

    private static boolean skipLib(final String includedClass) {
        try {
            URLClassLoaderFirst.class.getClassLoader().loadClass(includedClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private final ClassLoader system;

    public URLClassLoaderFirst(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        system = ClassLoader.getSystemClassLoader();
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        // already loaded?
        Class<?> clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        // JSE classes?
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException ignored) {
            // no-op
        }

        // look for it in this classloader
        boolean ok = !shouldSkip(name);
        if (ok) {
            clazz = loadInternal(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        // finally delegate
        clazz = loadFromParent(name, resolve);
        if (clazz != null) {
            return clazz;
        }

        if (!ok) {
            clazz = loadInternal(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        throw new ClassNotFoundException(name);
    }

    private Class<?> loadFromParent(final String name, final boolean resolve) {
        ClassLoader parent = getParent();
        if (parent == null) {
            parent = system;
        }
        try {
            final Class<?> clazz = Class.forName(name, false, parent);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }

    private Class<?> loadInternal(final String name, final boolean resolve) {
        try {
            final Class<?> clazz = findClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }

    public static boolean shouldSkip(final String name) { // TODO: enhance it
        if (FORCED_SKIP != null) {
            for (String prefix : FORCED_SKIP) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
        }
        if (FORCED_LOAD != null) {
            for (String prefix : FORCED_LOAD) {
                if (name.startsWith(prefix)) {
                    return false;
                }
            }
        }

        if (name.startsWith("java.")) return true;
        if (name.startsWith("javax.faces.")) return false;
        if (name.startsWith("javax.")) return true;
        if (name.startsWith("sun.")) return true;

        // can be provided in the webapp
        if (name.startsWith("javax.servlet.jsp.jstl")) return false;
        if (name.equals("org.apache.commons.logging.impl.LogFactoryImpl")) return false;

        // the followin block is classes which enrich webapp classloader
        if (name.startsWith("org.apache.webbeans.jsf")) return false;
        if (name.startsWith("org.apache.openejb.hibernate.")) return false;
        if (name.startsWith("org.apache.openejb.jpa.integration.")) return false;
        if (name.startsWith("org.apache.openejb.toplink.")) return false;
        if (name.startsWith("org.apache.openejb.eclipselink.")) return false;
        if (name.startsWith("org.apache.tomee.mojarra.")) return false;

        // don't stop on commons package since we don't bring all commons
        if (name.startsWith("org.apache.commons.beanutils")) return true;
        if (name.startsWith("org.apache.commons.cli")) return true;
        if (name.startsWith("org.apache.commons.codec")) return true;
        if (name.startsWith("org.apache.commons.collections")) return true;
        if (name.startsWith("org.apache.commons.dbcp")) return true;
        if (name.startsWith("org.apache.commons.digester")) return true;
        if (name.startsWith("org.apache.commons.jocl")) return true;
        if (name.startsWith("org.apache.commons.lang")) return true;
        if (name.startsWith("org.apache.commons.logging")) return true;
        if (name.startsWith("org.apache.commons.pool")) return true;
        if (name.startsWith("org.apache.commons.net") && SKIP_COMMONS_NET) return true;

        // was:
        /*
        if (name.startsWith("org.apache.openejb.jee.")) return true;
        if (name.startsWith("org.apache.openejb.api.")) return true;
        */
        if (name.startsWith("org.apache.openejb")) return true;

        if (name.startsWith("org.apache.bval.")) return true;
        if (name.startsWith("org.apache.openjpa.")) return true;
        if (name.startsWith("org.apache.derby.")) return true;
        if (name.startsWith("org.apache.xbean.")) return true;
        if (name.startsWith("javassist")) return true;
        if (name.startsWith("org.codehaus.swizzle")) return true;
        if (name.startsWith("org.w3c.dom")) return true;
        if (name.startsWith("org.apache.geronimo.")) return true;
        if (name.startsWith("com.sun.org.apache.")) return true;
        if (name.startsWith("org.apache.coyote")) return true;
        if (name.startsWith("org.quartz")) return true;
        if (name.startsWith("serp.bytecode")) return true;
        if (name.startsWith("org.apache.webbeans.")) return true;
        if (name.startsWith("org.eclipse.jdt.")) return true;

        if (name.startsWith("org.slf4j")) return true;

        if (name.startsWith("org.apache.log4j") && SKIP_LOG4J) return true;

        // myfaces-impl
        // a lot of other jar uses org.apache.myfaces as base package
        if (SKIP_MYFACES) {
            if (name.startsWith("org.apache.myfaces.shared")) return true;
            if (name.startsWith("org.apache.myfaces.ee6.")) return true;
            if (name.startsWith("org.apache.myfaces.lifecycle.")) return true;
            if (name.startsWith("org.apache.myfaces.renderkit.")) return true;
            if (name.startsWith("org.apache.myfaces.context.")) return true;
            if (name.startsWith("org.apache.myfaces.logging.")) return true;
            if (name.startsWith("org.apache.myfaces.component.")) return true;
            if (name.startsWith("org.apache.myfaces.application.")) return true;
            if (name.startsWith("org.apache.myfaces.config.")) return true;
            if (name.startsWith("org.apache.myfaces.event.")) return true;
            if (name.startsWith("org.apache.myfaces.taglib.")) return true;
            if (name.startsWith("org.apache.myfaces.resource.")) return true;
            if (name.startsWith("org.apache.myfaces.el.")) return true;
            if (name.startsWith("org.apache.myfaces.webapp.")) return true;
            if (name.startsWith("org.apache.myfaces.spi.")) return true;
            if (name.startsWith("org.apache.myfaces.convert.")) return true;
            if (name.startsWith("org.apache.myfaces.debug.")) return true;
            if (name.startsWith("org.apache.myfaces.util.")) return true;
            if (name.startsWith("org.apache.myfaces.view.")) return true;
        }

        if (name.startsWith("org.apache.catalina")) return true;
        if (name.startsWith("org.apache.jasper.")) return true;
        if (name.startsWith("org.apache.tomcat.")) return true;
        if (name.startsWith("org.apache.el.")) return true;
        if (name.startsWith("org.apache.jsp")) return true;
        if (name.startsWith("org.apache.naming")) return true;
        if (name.startsWith("org.apache.taglibs.")) return true;

        return false;
    }
}
