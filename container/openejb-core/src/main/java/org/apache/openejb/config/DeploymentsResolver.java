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
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.filter.ExcludeIncludeFilter;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.finder.filter.IncludeExcludeFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.openejb.util.URLs.toFile;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentsResolver implements DeploymentFilterable {
    private static final String EXCLUDE_INCLUDE_ORDER = SystemInstance.get().getOptions().get("openejb.exclude-include.order", "include-exclude");

    private static final Logger logger = DeploymentLoader.logger;

    public static void loadFrom(final Deployments dep, final FileUtils path, final List<URL> jarList) {

        if (dep.getDir() != null) {

            try {

                loadFromDir(dep, path, jarList);

            } catch (Files.FileDoesNotExistException e) {
                logger.warning("<Deployments dir=\"" + dep.getFile() + "\"> - " + e.getMessage());

            } catch (RuntimeException e) {
                final String message = "<Deployments dir=\"" + dep.getFile() + "\"> - " + e.getMessage();

                logger.error(message);
                throw new DeploymentsConfigurationException(message);
            }

        } else if (dep.getFile() != null) {

            try {

                loadFromFile(dep, path, jarList);

            } catch (RuntimeException e) {
                final String message = "<Deployments file=\"" + dep.getFile() + "\"> - " + e.getMessage();
                logger.error(message);
                throw new DeploymentsConfigurationException(message);
            }

        }
    }

    public static class DeploymentsConfigurationException extends RuntimeException {
        public DeploymentsConfigurationException(String message) {
            super(message);
        }
    }

    private static void loadFromFile(Deployments dep, FileUtils path, List<URL> jarList) {
        final File file = Files.path(path.getDirectory(), dep.getFile());

        Files.exists(file);
        Files.readable(file);
        Files.file(file);

        try {
            final URL url = file.toURI().toURL();
            if (!jarList.contains(url)) {
                jarList.add(url);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot convert file to URL: file="+file.getAbsolutePath(), e);
        }
    }

    private static void loadFromDir(Deployments dep, FileUtils path, List<URL> jarList) {
        final File dir = Files.path(path.getDirectory(), dep.getDir());

        Files.exists(dir);
        Files.readable(dir);
        Files.dir(dir);

        final Map<String, File> files = new LinkedHashMap<String, java.io.File>();
        for (File file : dir.listFiles()) {
            files.put(file.getAbsolutePath(), file);
        }

        // Ignore any unpacked versions
        for (File file : dir.listFiles()) {
            if (!isArchive(file)) continue;
            final String archive = file.getAbsolutePath();
            files.remove(archive.substring(0, archive.length() - 4));
        }

        for (File file : files.values()) {
            try {
                final URL url = file.toURI().toURL();

                if (!jarList.contains(url)) {
                    jarList.add(url);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot convert file to URL: file="+file.getAbsolutePath(), e);
            }
        }
    }

    private static boolean isArchive(File file) {
        if (!file.isFile()) return false;
        if (!file.getName().endsWith("ar")) return false;

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
    public static void loadFromClasspath(final FileUtils base, final List<URL> jarList, final ClassLoader classLoader) {
        final Options options = SystemInstance.get().getOptions();
        final String include = options.get(CLASSPATH_INCLUDE, ".*");
        final String exclude = options.get(CLASSPATH_EXCLUDE, "");
        final Set<RequireDescriptors> requireDescriptors = options.getAll(CLASSPATH_REQUIRE_DESCRIPTOR, RequireDescriptors.CLIENT);
        final boolean filterDescriptors = options.get(CLASSPATH_FILTER_DESCRIPTORS, false);
        final boolean filterSystemApps = options.get(CLASSPATH_FILTER_SYSTEMAPPS, true);

        try {
            UrlSet urlSet = new UrlSet(classLoader);

            urlSet = URLs.cullSystemJars(urlSet);

            // save the prefiltered list of jars before excluding system apps
            // so that we can choose not to filter modules with descriptors on the full list
            final UrlSet prefiltered = urlSet;

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

            final List<URL> urls = new ArrayList<URL>();
            final boolean isWindows = System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");

            for (final URL url : urlSet.getUrls()) {
                final String ef = (isWindows ? url.toExternalForm().toLowerCase() : url.toExternalForm());
                final URL u = new URL(ef);
                if (!urls.contains(u)) {
                    urls.add(u);
                }
            }

            final int size = urls.size();
            if (size == 0 && include.length() > 0) {
                logger.warning("No classpath URLs matched.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                return;
            } else if (size == 0 && (!filterDescriptors && prefiltered.getUrls().size() == 0)) {
                return;
            } else if (size < 20) {
                logger.debug("Inspecting classpath for applications: " + urls.size() + " urls.");
            } else {
                // Has the user allowed some module types to be discoverable via scraping?
                final boolean willScrape = requireDescriptors.size() < RequireDescriptors.values().length;

                if (size < 50 && willScrape) {
                    logger.info("Inspecting classpath for applications: " + urls.size() + " urls. Consider adjusting your exclude/include.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                } else if (willScrape) {
                    logger.warning("Inspecting classpath for applications: " + urls.size() + " urls.");
                    logger.warning("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                }
            }

            final long begin = System.currentTimeMillis();
            processUrls(urls, classLoader, requireDescriptors, base, jarList);
            final long end = System.currentTimeMillis();
            final long time = end - begin;

            UrlSet unchecked = new UrlSet();
            if (!filterDescriptors) {
                unchecked = NewLoaderLogic.applyBuiltinExcludes(prefiltered.exclude(urlSet));
                if (filterSystemApps) {
                    unchecked = unchecked.exclude(".*/openejb-[^/]+(.(jar|ear|war)(./)?|/target/classes/?)");
                }
                processUrls(unchecked.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), base, jarList);
            }

            if (logger.isDebugEnabled()) {
                final int urlCount = urlSet.getUrls().size() + unchecked.getUrls().size();
                logger.debug("URLs after filtering: " + urlCount);
                for (final URL url : urlSet.getUrls()) {
                    logger.debug("Annotations path: " + url);
                }
                for (final URL url : unchecked.getUrls()) {
                    logger.debug("Descriptors path: " + url);
                }
            }

            if (urls.size() == 0) return;

            if (time < 1000) {
                logger.debug("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 4000 || urls.size() < 3) {
                logger.info("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 10000) {
                logger.warning("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
                logger.warning("Consider adjusting your " + CLASSPATH_EXCLUDE + " and " + CLASSPATH_INCLUDE + " settings.  Current settings: exclude='" + exclude + "', include='" + include + "'");
            } else {
                logger.fatal("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                final List<String> list = new ArrayList<String>();
                for (final URL url : urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (final String url : list) {
                    logger.info("Matched: " + url);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }

    }

    public static void processUrls(final List<URL> urls, final ClassLoader classLoader, final Set<RequireDescriptors> requireDescriptors, final FileUtils base, final List<URL> jarList) {
        for (URL url : urls) {

            final String urlProtocol = url.getProtocol();
            //Currently, we only support jar and file protocol
            final boolean isValidURL = urlProtocol.equals("jar") || urlProtocol.equals("file");
            if (!isValidURL) {
                logger.warning("Unknown protocol " + urlProtocol);
                continue;
            }

            final Deployments deployment;
            String path = "";
            try {

                final DeploymentLoader deploymentLoader = new DeploymentLoader();

                final Class<? extends DeploymentModule> moduleType = deploymentLoader.discoverModuleType(url, classLoader, requireDescriptors);
                if (AppModule.class.isAssignableFrom(moduleType) || EjbModule.class.isAssignableFrom(moduleType) || PersistenceModule.class.isAssignableFrom(moduleType) || ConnectorModule.class.isAssignableFrom(moduleType) || ClientModule.class.isAssignableFrom(moduleType)) {

                    if (AppModule.class.isAssignableFrom(moduleType) || ConnectorModule.class.isAssignableFrom(moduleType)) {

                        deployment = JaxbOpenejb.createDeployments();

                        if (urlProtocol.equals("jar")) {
                            url = new URL(url.getFile().replaceFirst("!.*$", ""));
                            final File file = toFile(url);
                            path = file.getAbsolutePath();
                            deployment.setFile(path);
                        } else if (urlProtocol.equals("file")) {
                            final File file = toFile(url);
                            path = file.getAbsolutePath();
                            deployment.setDir(path);
                        }

                        logger.info("Found " + moduleType.getSimpleName() + " in classpath: " + path);

                        loadFrom(deployment, base, jarList);
                    } else {
                        if (!jarList.contains(url)) {
                            jarList.add(url);
                        }
                    }

                }
            } catch (IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
            }
        }
    }
}
