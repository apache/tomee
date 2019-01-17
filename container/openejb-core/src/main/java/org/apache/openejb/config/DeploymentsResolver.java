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

package org.apache.openejb.config;

import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.filter.ExcludeIncludeFilter;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.finder.filter.IncludeExcludeFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

import static org.apache.openejb.util.URLs.toFile;
import static org.apache.openejb.util.URLs.toFileUrl;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentsResolver implements DeploymentFilterable {

    private static final String EXCLUDE_INCLUDE_ORDER = SystemInstance.get().getOptions().get("openejb.exclude-include.order", "include-exclude");
    private static final String[] ignoreDirs = SystemInstance.get().getProperty("openejb.ignore.directories", ".svn,_svn,cvs,.git,.hg").split(",");
    private static final Logger logger = DeploymentLoader.LOGGER;
    private static File lib;

    static {
        try {
            lib = SystemInstance.get().getHome().getDirectory("lib", false);
        } catch (final IOException e) {
            //Ignore
        }
    }

    public static boolean isExtractedDir(final File f) {
        if (new File(f.getParentFile(), f.getName() + ".war").exists()) {
            return true;
        }
        return new File(f.getParentFile(), f.getName() + ".ear").exists();
    }

    protected static boolean isValidDirectory(final File file) {

        if (file.isDirectory() && !file.isHidden() && !file.equals(lib)) {

            final String fn = file.getName();

            for (final String dir : ignoreDirs) {
                if (fn.equalsIgnoreCase(dir)) {
                    return false;
                }
            }

            final String[] files = file.list();

            return null != files && files.length > 0;
        }

        return false;
    }

    public static void loadFrom(final Deployments dep, final FileUtils path, final List<File> jarList) {

        if (dep.getDir() != null) {

            try {

                loadFromDir(dep, path, jarList);

            } catch (final Files.FileDoesNotExistException e) {
                logger.warning("File error: <Deployments dir=\"" + dep.getDir() + "\"> - " + e.getMessage());

            } catch (final RuntimeException e) {
                final String message = "Runtime error: <Deployments dir=\"" + dep.getDir() + "\"> - " + e.getMessage();

                logger.error(message);
                throw new DeploymentsConfigurationException(message);
            }

        } else if (dep.getFile() != null) {

            try {

                loadFromFile(dep, path, jarList);

            } catch (final RuntimeException e) {
                final String message = "<Deployments file=\"" + dep.getFile() + "\"> - " + e.getMessage();
                logger.error(message);
                throw new DeploymentsConfigurationException(message);
            }

        }
    }

    public static class DeploymentsConfigurationException extends RuntimeException {

        public DeploymentsConfigurationException(final String message) {
            super(message);
        }
    }

    private static void loadFromFile(final Deployments dep, final FileUtils path, final List<File> jarList) {
        final File file = Files.path(path.getDirectory(), dep.getFile());

        Files.exists(file);
        Files.readable(file);
        Files.file(file);

        if (!jarList.contains(file)) {
            jarList.add(file);
        }
    }

    private static void loadFromDir(final Deployments dep, final FileUtils path, final List<File> jarList) {
        final File dir = Files.path(path.getDirectory(), dep.getDir());

        Files.exists(dir);
        Files.readable(dir);
        Files.dir(dir);
        Files.notHidden(dir);

        final Map<String, File> files = new LinkedHashMap<>();
        final File[] list = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                if (f.isDirectory()) {
                    return DeploymentsResolver.isValidDirectory(f) && !DeploymentsResolver.isExtractedDir(f);
                }
                return true;
            }
        });

        if (list != null) {
            for (final File file : list) {

                files.put(file.getAbsolutePath(), file);
            }

            // Ignore any unpacked versions
            for (final File file : list) {
                if (!isArchive(file)) {
                    continue;
                }
                final String archive = file.getAbsolutePath();
                files.remove(archive.substring(0, archive.length() - 4));
            }
        }

        for (final File file : files.values()) {
            if (!jarList.contains(file)) {
                jarList.add(file);
            }
        }
    }

    private static boolean isArchive(final File file) {
        if (!file.isFile()) {
            return false;
        }
        if (!file.getName().toLowerCase().endsWith("ar")) {
            return false;
        }

        final String name = file.getName();
        final char c = name.charAt(name.length() - 4);
        return c == '.';
    }

    /**
     * The algorithm of OpenEJB deployments class-path inclusion and exclusion is implemented as follows:
     * 1- If the string value of the resource URL matches the include class-path pattern
     * Then load this resource
     * 2- If the string value of the resource URL matches the exclude class-path pattern
     * Then ignore this resource
     * 3- If the include and exclude class-path patterns are not defined
     * Then load this resource
     * <p/>
     * The previous steps are based on the following points:
     * 1- Include class-path pattern has the highest priority
     * This helps in case both patterns are defined using the same values.
     * This appears in step 1 and 2 of the above algorithm.
     * 2- Loading the resource is the default behaviour in case of not defining a value for any class-path pattern
     * This appears in step 3 of the above algorithm.
     */
    public static List<URL> loadFromClasspath(final ClassLoader classLoader) {
        final ClasspathSearcher searchResult = new ClasspathSearcher().loadUrls(classLoader);
        if (searchResult.prefiltered == null || searchResult.urlSet == null) { // an error occured
            return new ArrayList<>(); // allow iterator to fully work compared to emptyList()
        }

        try {
            final List<URL> jarList = new ArrayList<>(searchResult.urls.size());

            final int size = searchResult.urls.size();
            if (size == 0 && searchResult.include.length() > 0) {
                logger.warning("No classpath URLs matched.  Current settings: " +
                        CLASSPATH_EXCLUDE + "='" + searchResult.exclude + "', " +
                        CLASSPATH_INCLUDE + "='" + searchResult.include + "'");
                return jarList;
            } else if (size == 0 && !searchResult.filterDescriptors && searchResult.prefiltered.getUrls().size() == 0) {
                return jarList;
            } else if (size < 20) {
                logger.debug("Inspecting classpath for applications: " + size + " urls.");
            } else {
                // Has the user allowed some module types to be discoverable via scraping?
                final boolean willScrape = searchResult.requireDescriptors.size() < RequireDescriptors.values().length;

                if (size < 50 && willScrape) {
                    logger.info("Inspecting classpath for applications: " +
                            size +
                            " urls. Consider adjusting your exclude/include.  " +
                            "Current settings: " + CLASSPATH_EXCLUDE + "='" + searchResult.exclude + "', " +
                            CLASSPATH_INCLUDE + "='" + searchResult.include + "'");
                } else if (willScrape) {
                    logger.warning("Inspecting classpath for applications: " + size + " urls.");
                    logger.warning("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " +
                            CLASSPATH_EXCLUDE + "='" + searchResult.exclude + "', " +
                            CLASSPATH_INCLUDE + "='" + searchResult.include + "'");
                }
            }

            final long begin = System.currentTimeMillis();
            processUrls("DeploymentsResolver1", searchResult.urls, classLoader, searchResult.requireDescriptors, jarList);
            final long end = System.currentTimeMillis();
            final long time = end - begin;

            UrlSet unchecked = new UrlSet();

            if (!searchResult.filterDescriptors) {
                unchecked = NewLoaderLogic.applyBuiltinExcludes(searchResult.prefiltered.exclude(searchResult.prefiltered));
                if (searchResult.filterSystemApps) {
                    unchecked = unchecked.exclude(".*/openejb-[^/]+(.(jar|ear|war)(./)?|/target/classes/?)");
                }
                processUrls("DeploymentsResolver2", unchecked.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), jarList);
            }

            if (logger.isDebugEnabled()) {
                final int urlCount = searchResult.urlSet.getUrls().size() + unchecked.getUrls().size();
                logger.debug("DeploymentsResolver: URLs after filtering: " + urlCount);
                for (final URL url : searchResult.urlSet.getUrls()) {
                    logger.debug("Annotations path: " + url);
                }
                for (final URL url : unchecked.getUrls()) {
                    logger.debug("Descriptors path: " + url);
                }
            }

            final int urlSize = searchResult.urls.size();
            if (urlSize == 0) {
                return jarList;
            }

            if (time < 1000) {
                logger.debug("Searched " + urlSize + " classpath urls in " + time + " milliseconds.  Average " + time / urlSize + " milliseconds per url.");
            } else if (time < 4000 || urlSize < 3) {
                logger.info("Searched " + urlSize + " classpath urls in " + time + " milliseconds.  Average " + time / urlSize + " milliseconds per url.");
            } else if (time < 10000) {
                logger.warning("Searched " + urlSize + " classpath urls in " + time + " milliseconds.  Average " + time / urlSize + " milliseconds per url.");
                logger.warning("Consider adjusting your " +
                        CLASSPATH_EXCLUDE + " and " + CLASSPATH_INCLUDE + " settings.  Current settings: exclude='" +
                        searchResult.exclude + "', include='" + searchResult.include + "'");
            } else {
                logger.fatal("Searched " + urlSize + " classpath urls in " + time + " milliseconds.  Average " + time / urlSize + " milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " +
                        CLASSPATH_EXCLUDE + "='" + searchResult.exclude + "', " +
                        CLASSPATH_INCLUDE + "='" + searchResult.include + "'");
                final List<String> list = new ArrayList<>();
                for (final URL url : searchResult.urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (final String url : list) {
                    logger.info("Matched: " + url);
                }
            }
            return jarList;
        } catch (final IOException e1) {
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }
        return new ArrayList<>();
    }

    /**
     * Use {@link #loadFromClasspath(ClassLoader)}
     */
    @Deprecated
    public static void loadFromClasspath(final FileUtils ignored, final List<URL> jarList, final ClassLoader classLoader) {
        jarList.addAll(loadFromClasspath(classLoader));
    }

    /**
     * Use {@link #processUrls(String, List, ClassLoader, Set, List)}
     */
    @Deprecated
    public static void processUrls(final String caller,
                                   final List<URL> urls,
                                   final ClassLoader classLoader,
                                   final Set<RequireDescriptors> requireDescriptors,
                                   final FileUtils ignored, // don't use it, it will be removed since we already suppose it is null in several places
                                   final List<URL> jarList) {
        processUrls(caller, urls, classLoader, requireDescriptors, jarList);
    }

    public static void processUrls(final String caller,
                                   final List<URL> urls,
                                   final ClassLoader classLoader,
                                   final Set<RequireDescriptors> requireDescriptors,
                                   final List<URL> jarList) {
        for (final URL url : urls) {

            final String urlProtocol = url.getProtocol();
            //Currently, we only support jar and file protocol
            final boolean isValidURL = urlProtocol.equals("jar") || urlProtocol.equals("file");
            if (!isValidURL) {
                logger.warning("Unknown protocol " + urlProtocol);
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(caller + ".processing: " + url);
            }

            try {

                final DeploymentLoader deploymentLoader = new DeploymentLoader();

                final Class<? extends DeploymentModule> moduleType = deploymentLoader.discoverModuleType(url, classLoader, requireDescriptors);
                if (AppModule.class.isAssignableFrom(moduleType) ||
                        EjbModule.class.isAssignableFrom(moduleType) ||
                        PersistenceModule.class.isAssignableFrom(moduleType) ||
                        ConnectorModule.class.isAssignableFrom(moduleType) ||
                        ClientModule.class.isAssignableFrom(moduleType)) {

                    final URL archive = toFileUrl(url);

                    if (!jarList.contains(archive)) {
                        jarList.add(archive);
                        final File file = toFile(archive);
                        logger.info("Found " + moduleType.getSimpleName() + " in classpath: " + file.getAbsolutePath());
                    }
                }
            } catch (final IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (final UnknownModuleTypeException ignore) {
                // no-op
            }
        }
    }

    public static class ClasspathSearcher {
        // config used for scanning
        private final String include;
        private final String exclude;
        private final boolean filterSystemApps;
        private final Set<RequireDescriptors> requireDescriptors;
        private final boolean filterDescriptors;

        // set got
        private UrlSet urlSet;
        private UrlSet prefiltered;

        // final result
        private List<URL> urls;

        public ClasspathSearcher() {
            final Options options = SystemInstance.get().getOptions();
            include = options.get(CLASSPATH_INCLUDE, ".*");
            exclude = options.get(CLASSPATH_EXCLUDE, "");
            filterSystemApps = options.get(CLASSPATH_FILTER_SYSTEMAPPS, true);
            requireDescriptors = options.getAll(CLASSPATH_REQUIRE_DESCRIPTOR, RequireDescriptors.CLIENT);
            filterDescriptors = options.get(CLASSPATH_FILTER_DESCRIPTORS, false);
        }

        public List<URL> getUrls() {
            return urls;
        }

        private UrlSet cleanUpUrlSet(final UrlSet set) {
            if (set.size() >= 5) { // if set size == 1 then we use both getURLs() and getresource(META-INF) to find jar, ensure we don't duplicate it, ie size ~ 2
                return set;
            }

            final List<URL> copy = set.getUrls();
            for (final URL url : set.getUrls()) {
                try {
                    if ("file".equals(url.getProtocol()) && copy.contains(new URL("jar:" + url.toExternalForm() + "!/"))) {
                        copy.remove(url);
                    }
                } catch (final MalformedURLException e) {
                    // no-op
                }
            }
            return new UrlSet(copy);
        }

        public ClasspathSearcher loadUrls(final ClassLoader classLoader) {
            try {
                final UrlSet original = cleanUpUrlSet(new UrlSet(classLoader));
                urlSet = URLs.cullSystemJars(original);

                // save the prefiltered list of jars before excluding system apps
                // so that we can choose not to filter modules with descriptors on the full list
                prefiltered = urlSet;

                Filter includeFilter = Filters.patterns(include);

                // we should exclude system apps before and apply user properties after
                if (!".*".equals(include) || !"".equals(exclude)) { // if we are using default this will not do anything
                    // the next line should probably replaced by:
                    // final Filter filter = new ExcludeIncludeFilter(includeFilter, Filters.patterns(exclude));
                    final Filter filter;
                    if (EXCLUDE_INCLUDE_ORDER.startsWith("include")) { // this test should be simply enough
                        filter = new IncludeExcludeFilter(includeFilter, Filters.patterns(exclude));
                    } else {
                        filter = new ExcludeIncludeFilter(includeFilter, Filters.patterns(exclude));
                    }

                    // filter using user parameters
                    urlSet = urlSet.filter(filter);
                } else {
                    includeFilter = null;
                }

                if (prefiltered.size() == urlSet.size()) {
                    urlSet = NewLoaderLogic.applyBuiltinExcludes(urlSet, includeFilter);

                    if (filterSystemApps) {
                        urlSet = urlSet.exclude(".*/openejb-[^/]+(.(jar|ear|war)(!/)?|/target/(test-)?classes/?)");
                    }
                }

                final boolean isWindows = JavaSecurityManagers.getSystemProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH).startsWith("windows");
                if (!isWindows || !Boolean.parseBoolean(SystemInstance.get().getProperty("openejb.resolver.windows.lowercase-urls", "true"))) {
                    urls = urlSet.getUrls();
                } else {
                    urls = new ArrayList<>();
                    for (final URL url : urlSet.getUrls()) {
                        final String ef = url.toExternalForm().toLowerCase();
                        final URL u = new URL(ef);
                        if (!urls.contains(u)) {
                            urls.add(u);
                        }
                    }
                }

                return this;
            } catch (final IOException e1) {
                logger.warning("Unable to search classpath", e1);
            }
            return this;
        }
    }
}
