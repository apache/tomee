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

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PerformanceTimer;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.ExcludeIncludeFilter;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.finder.filter.IncludeExcludeFilter;
import org.apache.xbean.finder.filter.PatternFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class NewLoaderLogic {

    private static final Logger logger = DeploymentLoader.LOGGER;
    public static final String DEFAULT_EXCLUSIONS_ALIAS = "default-list";
    public static final String ADDITIONAL_EXCLUDES = SystemInstance.get().getOptions().get("openejb.additional.exclude", (String) null);
    public static final String ADDITIONAL_INCLUDE = SystemInstance.get().getOptions().get("openejb.additional.include", (String) null);
    public static final String EXCLUSION_FILE = "exclusions.list";

    private static String[] exclusions;
    private static volatile Filter filter;

    public static UrlSet filterArchives(final Filter filter, final ClassLoader classLoader, UrlSet urlSet) {

        for (final URL url : urlSet.getUrls()) {
            for (final Archive archive : ClasspathArchive.archives(classLoader, url)) {

                final FilteredArchive filtered = new FilteredArchive(archive, filter);

                if (!filtered.iterator().hasNext()) {
                    urlSet = urlSet.exclude(url);
                }

            }
        }

        return urlSet;
    }

    public static Set<String> callers() {
        return callers(Filters.classes("jakarta.ejb.embeddable.EJBContainer", "javax.naming.InitialContext"));
    }

    public static Set<String> callers(final Filter start) {

        final Set<String> callers = new LinkedHashSet<>();

        final List<StackTraceElement> elements = new ArrayList<>(Arrays.asList(new Exception().fillInStackTrace().getStackTrace()));

        // Yank out everything until we find a known ENTRY point
        // if we don't find one, so be it, this is only a convenience
        {
            final Iterator<StackTraceElement> iterator = elements.iterator();
            while (iterator.hasNext()) {
                final StackTraceElement element = iterator.next();
                iterator.remove();

                // If we haven't yet reached an entry point, just keep going
                if (!start.accept(element.getClassName())) {
                    continue;
                }

                // We found an entry point.
                // Fast-forward past this class
                while (iterator.hasNext() && element.getClassName().equals(iterator.next().getClassName())) {
                    iterator.remove();
                }

                // Ok, we have iterated up to the calling user class, so stop now
                break;
            }
        }

        // Now iterate till we find an END point
        // We don't want any of the classes after that
        {
            final Filter end = Filters.packages(
                "junit.",
                "org.junit.",
                "org.testng.",
                "org.apache.maven.",
                "org.eclipse.",
                "com.intellij.",
                "org.scalatest."
            );

            // Everything between here and the end is part
            // of the call chain in which we are interested
            for (final StackTraceElement element : elements) {
                if (end.accept(element.getClassName())) {
                    break;
                }

                callers.add(element.getClassName());
            }
        }

        // We don't need this anymore
        elements.clear();

        // Finally filter out everything that we definitely don't want
        {
            final Filter unwanted = Filters.packages(
                "java.",
                "javax.",
                "jakarta.",
                "sun.reflect."
            );

            callers.removeIf(unwanted::accept);
        }

        return callers;
    }

    public static UrlSet applyBuiltinExcludes(final UrlSet urlSet) throws MalformedURLException {
        return applyBuiltinExcludes(urlSet, null);
    }

    public static UrlSet applyBuiltinExcludes(final UrlSet urlSet, final Filter includeFilter) throws MalformedURLException {
        return applyBuiltinExcludes(urlSet, includeFilter, null);
    }

    public static boolean skip(final URL url) {
        return skip(url, null, null);
    }

    public static boolean skip(final URL url, final Filter includeFilter, final Filter excludeFilter) {
        if ("archive".equals(url.getProtocol())) {
            return true;
        }

        try {
            final File file = URLs.toFile(url);

            final String name = NameFiltering.filter(file).getName();

            if (skip(includeFilter, excludeFilter, name)) {
                return true;
            }
        } catch (final IllegalArgumentException iae) {
            // no-op
        }

        return false;
    }

    private static boolean skip(final Filter includeFilter, final Filter excludeFilter, final String name) {
        if (includeFilter == null || !includeFilter.accept(name)) {
            if (filter != null && filter.accept(name)) {
                return true;
            } else if (excludeFilter != null && excludeFilter.accept(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean skip(final String name) {
        getExclusions();
        if (filter != null && filter.accept(name)) {
            return true;
        }
        return false;
    }

    public static UrlSet applyBuiltinExcludes(final UrlSet urlSet, final Filter includeFilter, final Filter excludeFilter) throws MalformedURLException {
        getExclusions(); // force init

        final List<URL> urls = urlSet.getUrls();
        urls.removeIf(url -> skip(url, includeFilter, excludeFilter));

        return new UrlSet(urls);
    }

    @Deprecated
    public static void setExclusions(final String[] exclusionArray) {
        exclusions = exclusionArray;

        // reinit the filter
        filter = null;
        getFilter();

        logExclusions(exclusionArray);
    }

    /**
     * @param excluded a filter returning true for filtered jars.
     * @param included a filter returning true for included jars.
     */
    public static void addAdditionalCustomFilter(final Filter excluded, final Filter included) {
        getExclusions();

        // reinit the filter, we synchronized for consistency but there it should be thread safe anyway
        if (excluded != null && included != null) {
            synchronized (NewLoaderLogic.class) {
                final Filter builtIn = new OptimizedExclusionFilter(getExclusions());
                NewLoaderLogic.filter = new Filter() {
                    @Override
                    public boolean accept(final String name) {
                        return !included.accept(name) && (builtIn.accept(name) || excluded.accept(name));
                    }
                };
            }
        } else if (excluded != null) {
            synchronized (NewLoaderLogic.class) {
                final Filter builtIn = new OptimizedExclusionFilter(getExclusions());
                NewLoaderLogic.filter = new Filter() {
                    @Override
                    public boolean accept(final String name) {
                        return builtIn.accept(name) || excluded.accept(name);
                    }
                };
            }
        } else if (included != null) {
            synchronized (NewLoaderLogic.class) {
                final Filter builtIn = new OptimizedExclusionFilter(getExclusions());
                NewLoaderLogic.filter = new Filter() {
                    @Override
                    public boolean accept(final String name) {
                        return !included.accept(name) && builtIn.accept(name);
                    }
                };
            }
        }

        logExclusions(exclusions);
    }

    private static void logExclusions(final String[] exclusionArray) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exclusion prefixes: [");
            for (final String ex : exclusionArray) {
                logger.debug("-" + ex);
            }
            logger.debug("]");
        }
    }

    public static String[] getExclusions() {

        if (exclusions != null) {
            return exclusions;
        }

        FileInputStream fis = null;

        try {
            final File exclusionsFile = SystemInstance.get().getConf(EXCLUSION_FILE);
            if (exclusionsFile != null && exclusionsFile.exists()) {
                fis = new FileInputStream(exclusionsFile);
                exclusions = readInputStreamList(fis);

                logger.info("Loaded classpath exclusions from: " + exclusionsFile.getAbsolutePath());
            }
        } catch (final Throwable e) {
            // ignored
        } finally {
            IO.close(fis);
        }

        if (exclusions == null) {

            exclusions = readDefaultExclusions();
        }

        final List<String> excludes = new ArrayList<>(exclusions.length + 5);
        excludes.addAll(Arrays.asList(exclusions));

        if (ADDITIONAL_EXCLUDES != null) {
            Collections.addAll(excludes, ADDITIONAL_EXCLUDES.split("[ \t\r\n]*,[ \t\n\n]*"));
        }
        if (ADDITIONAL_INCLUDE != null) { // include = not excluded
            for (final String rawInclude : ADDITIONAL_INCLUDE.split("[ \t\n\n]*,[ \t\n\n]*")) {
                final String include = rawInclude.trim();
                excludes.removeIf(s -> s.startsWith(include));
            }
        }

        exclusions = excludes.toArray(new String[excludes.size()]);
        getFilter(); // ensure filter is initialized

        logExclusions(exclusions);

        return exclusions;
    }

    @SuppressWarnings("unchecked")
    public static Filter getFilter() {
        if (filter == null) {
            synchronized (NewLoaderLogic.class) {
                if (filter == null) {
                    filter = new OptimizedExclusionFilter(getExclusions());
                }
            }
        }
        return filter;
    }

    private static String[] readDefaultExclusions() {
        InputStream is = null;
        String[] read = null;
        try {
            is = NewLoaderLogic.class.getResourceAsStream("/default.exclusions");
            read = readInputStreamList(is);

            logger.debug("Loaded default.exclusions");

        } catch (final Throwable e) {
            // ignored
        } finally {
            IO.close(is);
        }

        return read;
    }

    public static String sanitize(final String value) {
        if (value.endsWith("*.jar")) {
            return value.substring(0, value.length() - 5);
        } else if (value.endsWith("*")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static String[] readInputStreamList(final InputStream is) {

        final List<String> list = new ArrayList<>();

        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            while ((line = reader.readLine()) != null) {
                final String value = line.trim();
                if (line.startsWith("#") || value.isEmpty()) {
                    continue;
                }

                if (DEFAULT_EXCLUSIONS_ALIAS.equals(value)) {
                    Collections.addAll(list, readDefaultExclusions());
                } else {
                    list.add(sanitize(value));
                }
            }
        } catch (final Throwable e) {
            logger.warning("readInputStreamList: Failed to read provided stream");
        }
        //Ignore

        return list.toArray(new String[list.size()]);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void _loadFromClasspath(final FileUtils base, final List<URL> jarList, final ClassLoader classLoader) {

        final PerformanceTimer timer = new PerformanceTimer();

        timer.event("create filters");
        final Options options = SystemInstance.get().getOptions();
        final String include = "";
        final String exclude = "";
        final PatternFilter classpathInclude = new PatternFilter(options.get(DeploymentFilterable.CLASSPATH_INCLUDE, ".*"));
        final PatternFilter classpathExclude = new PatternFilter(options.get(DeploymentFilterable.CLASSPATH_EXCLUDE, ""));
        final Filter classpathFilter = new ExcludeIncludeFilter(classpathInclude, classpathExclude);

        final PatternFilter packageInclude = new PatternFilter(options.get(DeploymentFilterable.PACKAGE_INCLUDE, ".*"));
        final PatternFilter packageExclude = new PatternFilter(options.get(DeploymentFilterable.PACKAGE_EXCLUDE, ""));

        final IncludeExcludeFilter packageFilter;
        if (classpathInclude.getPattern().pattern().equals(".*") && packageInclude.getPattern().pattern().equals(".*")) {

            timer.event("callers");

            final Set<String> callers = callers();

            timer.event("parse packages");

            callers.size();

            final Set<String> packages = new HashSet<>();
            for (final String caller : callers) {
                String[] parts = caller.split("\\.");
                if (parts.length > 2) {
                    parts = new String[]{parts[0], parts[1]};
                }
                packages.add(Join.join(".", parts));
            }

            final Filter includes = Filters.packages(packages.toArray(new String[packages.size()]));

            packageFilter = new IncludeExcludeFilter(includes, packageExclude);

        } else {

            packageFilter = new IncludeExcludeFilter(packageInclude, packageExclude);

        }

        timer.event("urlset");

        final Set<RequireDescriptors> requireDescriptors = options.getAll(DeploymentFilterable.CLASSPATH_REQUIRE_DESCRIPTOR, RequireDescriptors.CLIENT);
        try {
            UrlSet urlSet = new UrlSet(classLoader);

            timer.event("exclude system urls");
            urlSet = URLs.cullSystemJars(urlSet);

            timer.event("classpath filter");

            final UrlSet beforeFiltering = urlSet;

            urlSet = urlSet.filter(classpathFilter);

            // If the user filtered out too much, that's a problem
            if (urlSet.size() == 0) {
                final String message = String.format("Classpath Include/Exclude resulted in zero URLs.  There were %s possible URLs before filtering and 0 after: include=\"%s\", exclude=\"%s\"",
                    beforeFiltering.size(),
                    include,
                    exclude);
                logger.error(message);
                logger.info("Eligible Classpath before filtering:");

                for (final URL url : beforeFiltering) {
                    logger.info(String.format("   %s", url.toExternalForm()));
                }
            }

            // If they are the same size, than nothing was filtered
            // and we know the user did not take action to change the default
            final boolean userSuppliedClasspathFilter = beforeFiltering.size() != urlSet.size();

            if (!userSuppliedClasspathFilter) {

                logger.info("Applying buildin classpath excludes");
                timer.event("buildin excludes");
                urlSet = applyBuiltinExcludes(urlSet);

            }

            DeploymentsResolver.processUrls("NewLoaderLogic1", urlSet.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), base, jarList);

            timer.event("package filter");

            urlSet = filterArchives(packageFilter, classLoader, urlSet);

            timer.event("process urls");

            final List<URL> urls = urlSet.getUrls();

            final long begin = System.currentTimeMillis();
            DeploymentsResolver.processUrls("NewLoaderLogic2", urls, classLoader, requireDescriptors, base, jarList);
            final long end = System.currentTimeMillis();
            final long time = end - begin;

            timer.stop(System.out);

            final UrlSet unchecked = new UrlSet();
            DeploymentsResolver.processUrls("NewLoaderLogic3", unchecked.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), base, jarList);

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

            if (urls.size() == 0) {
                return;
            }

            if (time < 1000) {
                logger.debug("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + time / urls.size() + " milliseconds per url.");
            } else if (time < 4000 || urls.size() < 3) {
                logger.info("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + time / urls.size() + " milliseconds per url.");
            } else if (time < 10000) {
                logger.warning("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + time / urls.size() + " milliseconds per url.");
                logger.warning("Consider adjusting your " +
                    DeploymentFilterable.CLASSPATH_EXCLUDE +
                    " and " +
                    DeploymentFilterable.CLASSPATH_INCLUDE +
                    " settings.  Current settings: exclude='" +
                    exclude +
                    "', include='" +
                    include +
                    "'");
            } else {
                logger.fatal("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + time / urls.size() + " milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " +
                    DeploymentFilterable.CLASSPATH_EXCLUDE +
                    "='" +
                    exclude +
                    "', " +
                    DeploymentFilterable.CLASSPATH_INCLUDE +
                    "='" +
                    include +
                    "'");
                final List<String> list = new ArrayList<>();
                for (final URL url : urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (final String url : list) {
                    logger.info("Matched: " + url);
                }
            }
        } catch (final IOException e1) {
            e1.printStackTrace();
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }

    }

    public static class OptimizedExclusionFilter implements Filter {
        private final Set<String> included = new HashSet<>();

        public OptimizedExclusionFilter(final String[] exclusions) {
            included.addAll(asList(exclusions));
            for (final String e : exclusions) {
                if (e.endsWith("-")) {
                    included.add(e.substring(0, e.length() - 1) + ".jar");
                }
            }
        }

        @Override
        public boolean accept(final String name) {
            for (int i = 1; i <= name.length(); i++) {
                if (included.contains(name.substring(0, i))) {
                    return true;
                }
            }
            return false;
        }
    }
}
