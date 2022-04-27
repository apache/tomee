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

package org.apache.openejb.util.classloader;

import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;

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
    private static final boolean SKIP_HSQLDB = skipLib("org.hsqldb.lib.HsqlTimer");
    // commons-net is only in tomee-plus
    private static final boolean SKIP_COMMONS_NET = skipLib("org.apache.commons.net.pop3.POP3Client");

    // first skip container APIs if not in the jaxrs or plus version
    private static final boolean SKIP_JAXWS = skipLib("org.apache.cxf.jaxws.support.JaxWsImplementorInfo");
    private static final boolean SKIP_JMS = skipLib("org.apache.activemq.broker.BrokerFactory");
    private static final boolean EMBEDDED = "true".equals(SystemInstance.get().getProperty("openejb.embedded"));

    // - will not match anything, that's the desired default behavior
    public static final Collection<String> FORCED_SKIP = new ArrayList<>();
    public static final Collection<String> FORCED_LOAD = new ArrayList<>();
    public static final Collection<String> FILTERABLE_RESOURCES = new ArrayList<>();

    static {
        reloadConfig();
        ClassLoader.registerAsParallelCapable();
    }

    public static final String SLF4J_BINDER_CLASS = "org/slf4j/impl/StaticLoggerBinder.class";
    private static final URL SLF4J_CONTAINER = URLClassLoaderFirst.class.getClassLoader().getResource(SLF4J_BINDER_CLASS);
    private static final String CLASS_EXT = ".class";
    public static final ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private static final boolean ALLOW_OPEN_EJB_SYSTEM_LOADING = !Boolean.getBoolean("openejb.classloader.first.disallow-system-loading");

    public static void reloadConfig() {
        list(FORCED_SKIP, "openejb.classloader.forced-skip", null);
        list(FORCED_LOAD, "openejb.classloader.forced-load", null);
        list(FILTERABLE_RESOURCES, "openejb.classloader.filterable-resources",
                "META-INF/services/javax.validation.spi.ValidationProvider," +
                "META-INF/services/javax.ws.rs.client.ClientBuilder," +
                "META-INF/services/javax.json.spi.JsonProvider," +
                "META-INF/services/javax.cache.spi.CachingProvider," +
                "META-INF/javamail.default.providers," +
                "META-INF/javamail.default.address.map," +
                "META-INF/javamail.charset.map,META-INF/mailcap," +
                SLF4J_BINDER_CLASS);
    }

    private static void list(final Collection<String> list, final String key, final String def) {
        list.clear();

        final String s = SystemInstance.get().getOptions().get(key, def);
        if (s != null && !s.trim().isEmpty()) {
            list.addAll(Arrays.asList(s.trim().split(",")));
        }
    }

    private static boolean skipLib(final String includedClass) {
        try {
            URLClassLoaderFirst.class.getClassLoader().loadClass(includedClass);
            return "true".equalsIgnoreCase(JavaSecurityManagers.getSystemProperty(includedClass + ".skip", "true"));
        } catch (final ClassNotFoundException e) {
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
        synchronized (getClassLoadingLock(name)) {
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
                } catch (final NoClassDefFoundError | ClassNotFoundException ignored) {
                    // no-op
                }
            }

            // look for it in this classloader
            final boolean ok = !(shouldSkip(name) || shouldDelegateToTheContainer(this, name));
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
    }

    public static boolean shouldDelegateToTheContainer(final ClassLoader loader, final String name) {
        return shouldSkipJsf(loader, name) || shouldSkipSlf4j(loader, name);
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
        } catch (final ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }

    public Class<?> findAlreadyLoadedClass(final String name) {
        return super.findLoadedClass(name);
    }

    public Class<?> loadInternal(final String name, final boolean resolve) {
        try {
            final Class<?> clazz = findClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (final ClassNotFoundException ignored) {
            // no-op
        }
        return null;
    }

    // we skip webapp enrichment jars since we want to load them from the webapp or lib
    // Note: this is not a real limitation since it is first fail it will be done later
    public static boolean canBeLoadedFromSystem(final String name) {
        return ALLOW_OPEN_EJB_SYSTEM_LOADING && (!name.startsWith("org.apache.openejb.") || !isWebAppEnrichment(name.substring("org.apache.openejb.".length())));
    }

    // making all these call inline if far more costly than factorizing packages
    //
    // /!\ please check org.apache.openejb.persistence.PersistenceUnitInfoImpl.isServerClass() too
    // when updating this method
    public static boolean shouldSkip(final String input) {

        String name = input;

        if (name == null) { // can happen with rest servlet definition or errors
            return false;
        }

        for (final String prefix : FORCED_SKIP) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        for (final String prefix : FORCED_LOAD) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }

        if (name.startsWith("openejb.shade.")) {
            name = name.substring("openejb.shade.".length());
        }

        if (name.startsWith("java.")) {
            return true;
        }
        if (name.startsWith("javax.faces.")) {
            return false;
        }
        if (name.startsWith("javax.mail.")) {
            return false;
        }
        if (name.startsWith("javax.") || name.startsWith("jakarta.")) {
            return isInServer(name);
        }
        if (name.startsWith("sun.")) {
            return isInJvm(name);
        }

        // can be provided in the webapp
        if (name.startsWith("javax.servlet.jsp.jstl")) {
            return false;
        }

        if (name.startsWith("org.")) {
            final String org = name.substring("org.".length());

            if (org.startsWith("apache.")) {
                final String apache = org.substring("apache.".length());

                // the following block is classes which enrich webapp classloader
                if (apache.startsWith("webbeans.jsf")) {
                    return false;
                }
                if (apache.startsWith("tomee.mojarra.")) {
                    return false;
                }

                // here we find server classes
                if (apache.startsWith("bval.")) {
                    return true;
                }
                if (apache.startsWith("openjpa.")) {
                    return true;
                }
                if (apache.startsWith("xbean.")) {
                    return !apache.substring("xbean.".length()).startsWith("spring");
                }
                if (apache.startsWith("geronimo.")) {
                    return true;
                }
                if (apache.startsWith("coyote.")) {
                    return true;
                }
                if (apache.startsWith("webbeans.")) {
                    return true;
                }
                if (apache.startsWith("log4j.") && SKIP_LOG4J) {
                    return true;
                }
                if (apache.startsWith("catalina.")) {
                    return true;
                }
                if (apache.startsWith("jasper.")) {
                    return true;
                }
                if (apache.startsWith("tomcat.")) {
                    return true;
                }
                if (apache.startsWith("el.")) {
                    return true;
                }
                // if (apache.startsWith("jsp")) return true; // precompiled jsp have to be loaded from the webapp
                if (apache.startsWith("naming.")) {
                    return true;
                }
                if (apache.startsWith("taglibs.standard.")) {
                    return true;
                }

                if (apache.startsWith("openejb.")) { // skip all excepted webapp enrichment artifacts
                    return !isWebAppEnrichment(apache.substring("openejb.".length()));
                }

                if (apache.startsWith("commons.")) {
                    final String commons = apache.substring("commons.".length());

                    // don't stop on commons package since we don't bring all commons
                    if (commons.startsWith("beanutils.")) {
                        return isInServer(name);
                    }
                    if (commons.startsWith("cli.")) {
                        return true;
                    }
                    if (commons.startsWith("codec.")) {
                        return true;
                    }
                    if (commons.startsWith("collections.")) {
                        return true;
                    }
                    if (commons.startsWith("dbcp.")) {
                        return true;
                    }
                    if (commons.startsWith("dbcp2.")) {
                        return true;
                    }
                    if (commons.startsWith("digester.")) {
                        return true;
                    }
                    if (commons.startsWith("jocl.")) {
                        return true;
                    }
                    if (commons.startsWith("lang.")) { // openjpa
                        return true;
                    }
                    if (commons.startsWith("lang3.")) {  // us
                        return true;
                    }
                    if (commons.startsWith("logging.")) {
                        return false;
                    }
                    if (commons.startsWith("pool.")) {
                        return true;
                    }
                    if (commons.startsWith("pool2.")) {
                        return true;
                    }
                    if (commons.startsWith("net.") && SKIP_COMMONS_NET) {
                        return true;
                    }

                    return false;
                }

                if (SKIP_MYFACES && apache.startsWith("myfaces.")) {
                    // we bring only myfaces-impl (+api but that's javax)
                    // mainly inspired from a comparison with tomahawk packages
                    final String myfaces = name.substring("myfaces.".length());
                    if (myfaces.startsWith("shared.")) {
                        return true;
                    }
                    if (myfaces.startsWith("ee.")) {
                        return true;
                    }
                    if (myfaces.startsWith("lifecycle.")) {
                        return true;
                    }
                    if (myfaces.startsWith("context.")) {
                        return true;
                    }
                    if (myfaces.startsWith("logging.")) {
                        return true;
                    }
                    // tomahawk uses component.html package
                    if (myfaces.startsWith("component.visit.") || myfaces.equals("component.ComponentResourceContainer")) {
                        return true;
                    }
                    if (myfaces.startsWith("application.")) {
                        return true;
                    }
                    if (myfaces.startsWith("config.")) {
                        return true;
                    }
                    if (myfaces.startsWith("event.")) {
                        return true;
                    }

                    if (myfaces.startsWith("resource.")) {
                        return true;
                    }
                    if (myfaces.startsWith("el.")) {
                        return true;
                    }
                    if (myfaces.startsWith("spi.")) {
                        return true;
                    }
                    if (myfaces.startsWith("convert.")) {
                        return true;
                    }
                    if (myfaces.startsWith("debug.")) {
                        return true;
                    }
                    if (myfaces.startsWith("util.")) {
                        return true;
                    }
                    if (myfaces.startsWith("view.")) {
                        return true;
                    }
                    if (myfaces.equals("convert.ConverterUtils")) {
                        return true;
                    }

                    if (myfaces.startsWith("renderkit.")) {
                        final String renderkit = myfaces.substring("renderkit.".length());
                        if (renderkit.startsWith("html.Html")) {
                            return true;
                        }
                        final char firstNextletter = renderkit.charAt(0);
                        if (Character.isUpperCase(firstNextletter)) {
                            return true;
                        }
                        return false;
                    }

                    if (myfaces.startsWith("taglib.")) {
                        final String taglib = myfaces.substring("taglib.".length());
                        if (taglib.startsWith("html.Html")) {
                            return true;
                        }
                        if (taglib.startsWith("core.")) {
                            return true;
                        }
                        return false;
                    }

                    if (myfaces.startsWith("webapp.")) {
                        final String webapp = myfaces.substring("webapp.".length());
                        if (webapp.startsWith("Faces")) {
                            return true;
                        }
                        if (webapp.startsWith("Jsp")) {
                            return true;
                        }
                        if (webapp.startsWith("Startup")) {
                            return true;
                        }
                        if (webapp.equals("AbstractFacesInitializer")) {
                            return true;
                        }
                        if (webapp.equals("MyFacesServlet")) {
                            return true;
                        }
                        if (webapp.equals("ManagedBeanDestroyerListener")) {
                            return true;
                        }
                        if (webapp.equals("WebConfigParamsLogger")) {
                            return true;
                        }
                        return false;
                    }

                    return false;
                }

                if (apache.startsWith("activemq.")) {
                    return SKIP_JMS && isInServer(name);
                }

                return false;
            }

            // other org packages
            if (org.startsWith("hsqldb.") && SKIP_HSQLDB) {
                return true;
            }
            if (org.startsWith("codehaus.swizzle.")) {
                final String swizzle = org.substring("codehaus.swizzle.".length());
                if (swizzle.startsWith("stream.")) {
                    return true;
                }
                if (swizzle.startsWith("rss.")) {
                    return true;
                }
                if (swizzle.startsWith("Grep.class") || swizzle.startsWith("Lexer.class")) {
                    return true;
                }
                return false;
            }
            if (org.startsWith("w3c.dom.") || org.startsWith("xml.sax.")) {
                return isInJvm(name);
            }
            if (org.startsWith("eclipse.jdt.")) {
                return true;
            }

            // let an app use its own slf4j impl (so its own api too)
            // if (org.startsWith("slf4j")) return true;

            return false;
        }

        // other packages
        if (name.startsWith("com.")) {
            final String sub = name.substring("com.".length());
            if (sub.startsWith("sun.")) {
                return !name.startsWith("sun.mail.") && isInJvm(name);
            }
            if (sub.startsWith("oracle.")) {
                return true;
            }
        }
        if (name.startsWith("jdk.")) {
            return true;
        }

        if (name.startsWith("serp.bytecode.")) {
            return true;
        }

        return false;
    }

    private static boolean isInJvm(final String name) {
        return SYSTEM_CLASS_LOADER.getResource(name.replace('.', '/') + CLASS_EXT) != null;
    }

    private static boolean isInServer(final String name) {
        if (name.startsWith("javax.")) {
            final String sub = name.substring("javax.".length());
            if (sub.startsWith("jws.")) {
                return SKIP_JAXWS || EMBEDDED;
            }
            if (sub.startsWith("jms.")) {
                return SKIP_JMS || EMBEDDED;
            }
        }
        if (name.startsWith("jakarta.")) {
            final String sub = name.substring("jakarta.".length());
            if (sub.startsWith("jws.")) {
                return SKIP_JAXWS || EMBEDDED;
            }
            if (sub.startsWith("jms.")) {
                return SKIP_JMS || EMBEDDED;
            }
        }
        return ParentClassLoaderFinder.Helper.get().getResource(name.replace('.', '/') + ".class") != null;
    }

    public static boolean shouldSkipJsf(final ClassLoader loader, final String name) {
        if (!name.startsWith("javax.faces.")) {
            return false;
        }

        // using annotation to test to avoid to load more classes with deps
        final String testClass;
        // these test classes have to be jsf 2.x AND 1.x otherwise we force JSF 2
        if ("javax.faces.webapp.FacesServlet".equals(name)) {
            testClass = "javax.faces.FactoryFinder";
        } else {
            testClass = "javax.faces.webapp.FacesServlet";
        }

        final String classname = testClass.replace('.', '/') + ".class";
        try {
            final Enumeration<URL> resources = loader.getResources(classname);
            final Collection<URL> thisJSf = Collections.list(resources);
            return thisJSf.isEmpty() || thisJSf.size() <= 1;
        } catch (final IOException e) {
            return true;
        }

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
        // currently bean validation, Slf4j, myfaces (because of enrichment)
        return name != null
            && (FILTERABLE_RESOURCES.contains(name) || name.startsWith("META-INF/services/org.apache.myfaces.spi"));
    }

    public static boolean shouldSkipSlf4j(final ClassLoader loader, final String name) {
        if (name == null || !name.startsWith("org.slf4j.")) {
            return false;
        }

        try { // using getResource here just returns randomly the container one so we need getResources
            final Enumeration<URL> resources = loader.getResources(SLF4J_BINDER_CLASS);
            while (resources.hasMoreElements()) {
                final URL resource = resources.nextElement();
                if (!resource.equals(SLF4J_CONTAINER)) {
                    // applicative slf4j
                    return false;
                }
            }
        } catch (final Throwable e) {
            // no-op
        }

        return true;
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
