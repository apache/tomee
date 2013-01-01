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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

// TODO: look SM usage, find a better name
public class URLClassLoaderFirst extends URLClassLoader {
    // log4j is optional, moreover it will likely not work if not skipped and loaded by a temp classloader
    private static final boolean SKIP_LOG4J = "true".equals(SystemInstance.get().getProperty("openejb.skip.log4j", "true")) && skipLib("org.apache.log4j.Logger");
    private static final boolean SKIP_MYFACES = "true".equals(SystemInstance.get().getProperty("openejb.skip.myfaces", "true")) && skipLib("org.apache.myfaces.spi.FactoryFinderProvider");
    // commons-net is only in tomee-plus
    private static final boolean SKIP_COMMONS_NET = skipLib("org.apache.commons.net.pop3.POP3Client");

    // - will not match anything, that's the desired default behavior
    public static final Collection<String> FORCED_SKIP = new ArrayList<String>();
    public static final Collection<String> FORCED_LOAD = new ArrayList<String>();

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
        if (canBeLoadedFromSystem(name)) {
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

    // we skip webapp enrichment jars since we want to load them from the webapp or lib
    // Note: this is not a real limitation since it is first fail it will be done later
    public static boolean canBeLoadedFromSystem(final String name) {
        return !name.startsWith("org.apache.openejb.") || !isWebAppEnrichment(name.substring("org.apache.openejb.".length()));
    }

    // making all these call inline if far more costly than factorizing packages
    //
    // /!\ please check org.apache.openejb.persistence.PersistenceUnitInfoImpl.isServerClass() too
    // when updating this method
    public static boolean shouldSkip(final String name) {
        if (name == null) { // can happen with rest servlet definition or errors
            return false;
        }

        for (String prefix : FORCED_SKIP) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        for (String prefix : FORCED_LOAD) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }

        if (name.startsWith("java.")) return true;
        if (name.startsWith("javax.faces.")) return false;
        if (name.startsWith("javax.")) return true;
        if (name.startsWith("sun.")) return true;

        // can be provided in the webapp
        if (name.startsWith("javax.servlet.jsp.jstl")) return false;

        if (name.startsWith("org.")) {
            final String org = name.substring("org.".length());

            if (org.startsWith("apache.")) {
                final String apache = org.substring("apache.".length());

                // the following block is classes which enrich webapp classloader
                if (apache.startsWith("webbeans.jsf")) return false;
                if (apache.startsWith("tomee.mojarra.")) return false;

                // here we find server classes
                if (apache.startsWith("bval.")) return true;
                if (apache.startsWith("openjpa.")) return true;
                if (apache.startsWith("derby.")) return true;
                if (apache.startsWith("xbean.")) return true;
                if (apache.startsWith("geronimo.")) return true;
                if (apache.startsWith("coyote")) return true;
                if (apache.startsWith("webbeans.")) return true;
                if (apache.startsWith("log4j") && SKIP_LOG4J) return true;
                if (apache.startsWith("catalina")) return true;
                if (apache.startsWith("jasper.")) return true;
                if (apache.startsWith("tomcat.")) return true;
                if (apache.startsWith("el.")) return true;
                if (apache.startsWith("jsp")) return true;
                if (apache.startsWith("naming")) return true;
                if (apache.startsWith("taglibs.")) return true;

                if (apache.startsWith("openejb.")) { // skip all excepted webapp enrichment artifacts
                    return !isWebAppEnrichment(apache.substring("openejb.".length()));
                }

                if (apache.startsWith("commons.")) {
                    final String commons = apache.substring("commons.".length());

                    // don't stop on commons package since we don't bring all commons
                    if (commons.startsWith("beanutils")) return true;
                    if (commons.startsWith("cli")) return true;
                    if (commons.startsWith("codec")) return true;
                    if (commons.startsWith("collections")) return true;
                    if (commons.startsWith("dbcp")) return true;
                    if (commons.startsWith("digester")) return true;
                    if (commons.startsWith("jocl")) return true;
                    if (commons.startsWith("lang")) return true;
                    if (commons.startsWith("logging")) return false;
                    if (commons.startsWith("pool")) return true;
                    if (commons.startsWith("net") && SKIP_COMMONS_NET) return true;

                    return false;
                }

                if (SKIP_MYFACES && apache.startsWith("myfaces.")) {
                    // we bring only myfaces-impl (+api but that's javax)
                    // mainly inspired from a comparison with tomahawk packages
                    final String myfaces = name.substring("myfaces.".length());
                    if (myfaces.startsWith("shared")) return true;
                    if (myfaces.startsWith("ee6.")) return true;
                    if (myfaces.startsWith("lifecycle.")) return true;
                    if (myfaces.startsWith("context.")) return true;
                    if (myfaces.startsWith("logging.")) return true;
                    // tomahawk uses component.html package
                    if (myfaces.startsWith("component.visit.") || myfaces.equals("component.ComponentResourceContainer")) return true;
                    if (myfaces.startsWith("application.")) return true;
                    if (myfaces.startsWith("config.")) return true;
                    if (myfaces.startsWith("event.")) return true;

                    if (myfaces.startsWith("resource.")) return true;
                    if (myfaces.startsWith("el.")) return true;
                    if (myfaces.startsWith("spi.")) return true;
                    if (myfaces.startsWith("convert.")) return true;
                    if (myfaces.startsWith("debug.")) return true;
                    if (myfaces.startsWith("util.")) return true;
                    if (myfaces.startsWith("view.")) return true;
                    if (myfaces.equals("convert.ConverterUtils")) return true;

                    if (myfaces.startsWith("renderkit.")) {
                        final String renderkit = myfaces.substring("renderkit.".length());
                        if (renderkit.startsWith("html.Html")) return true;
                        char firstNextletter = renderkit.charAt(0);
                        if (Character.isUpperCase(firstNextletter)) return true;
                        return false;
                    }

                    if (myfaces.startsWith("taglib.")) {
                        final String taglib = myfaces.substring("taglib.".length());
                        if (taglib.startsWith("html.Html")) return true;
                        if (taglib.startsWith("core.")) return true;
                        return false;
                    }

                    if (myfaces.startsWith("webapp.")) {
                        final String webapp = myfaces.substring("webapp.".length());
                        if (webapp.startsWith("Faces")) return true;
                        if (webapp.startsWith("Jsp")) return true;
                        if (webapp.startsWith("Startup")) return true;
                        if (webapp.equals("AbstractFacesInitializer")) return true;
                        if (webapp.equals("MyFacesServlet")) return true;
                        if (webapp.equals("ManagedBeanDestroyerListener")) return true;
                        if (webapp.equals("WebConfigParamsLogger")) return true;
                        return false;
                    }

                    return false;
                }

                return false;
            }

            // other org packages
            if (org.startsWith("codehaus.swizzle")) return true;
            if (org.startsWith("w3c.dom")) return true;
            if (org.startsWith("quartz")) return true;
            if (org.startsWith("eclipse.jdt.")) return true;

            // let an app use its own slf4j impl (so its own api too)
            // if (org.startsWith("slf4j")) return true;

            return false;
        }

        // other packages
        if (name.startsWith("com.sun.org.apache.")) return true;
        if (name.startsWith("javassist")) return true;
        if (name.startsWith("serp.bytecode")) return true;

        return false;
    }

    // in org.apache.openejb.
    private static boolean isWebAppEnrichment(final String openejb) {
        return openejb.startsWith("hibernate.") || openejb.startsWith("jpa.integration.")
                || openejb.startsWith("toplink.") || openejb.startsWith("eclipselink.")
                || openejb.startsWith("arquillian.");
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return URLClassLoaderFirst.filterResources(name, super.getResources(name));
    }

    public static boolean isFilterableResource(final String name) {
        // currently bean validation, Slf4j
        return "META-INF/services/javax.validation.spi.ValidationProvider".equals(name)
                || "org/slf4j/impl/StaticLoggerBinder.class".equals(name);
    }

    // useful method for SPI
    public static Enumeration<URL> filterResources(final String name, final Enumeration<URL> result) {
        if (isFilterableResource(name)) {
            final Collection<URL> values = Collections.list(result);
            if (values.size() > 1) {
                // remove openejb one
                final URL url = URLClassLoaderFirst.class.getResource("/" + name);
                if (url != null) {
                    values.remove(url);
                }
            }
            return Collections.enumeration(values);
        }
        return result;
    }
}
